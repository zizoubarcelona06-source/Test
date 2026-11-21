const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds
} = require('@firebase/rules-unit-testing');
const firebase = require(
  require.resolve('firebase/compat/app', {
    paths: ['/usr/local/lib/node_modules/@firebase/rules-unit-testing/node_modules']
  })
);
require(
  require.resolve('firebase/compat/firestore', {
    paths: ['/usr/local/lib/node_modules/@firebase/rules-unit-testing/node_modules']
  })
);

const PROJECT_ID = 'gen-lang-client-0051515797';
const { serverTimestamp } = firebase.firestore.FieldValue;
const { Timestamp } = firebase.firestore;

let testEnv;

test.before(async () => {
  const rules = fs.readFileSync('firestore.rules', 'utf8');
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      host: '127.0.0.1',
      port: 8085,
      rules
    }
  });
});

test.after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

test.beforeEach(async () => {
  await testEnv.clearFirestore();
});

function dbFor(uid) {
  return testEnv.authenticatedContext(uid).firestore();
}

function unauthDb() {
  return testEnv.unauthenticatedContext().firestore();
}

async function seedCompanyWorkspace(companyId = 'comp_btp_1', inviteCode = 'BTP7K9M2') {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = ctx.firestore();
    const now = Timestamp.now();
    await adminDb.collection('companies').doc(companyId).set({
      name: 'شركة البنيان للأشغال العمومية',
      currencySymbol: 'د.ج',
      inviteCode,
      ownerId: 'uid_admin',
      memberUids: ['uid_admin', 'uid_eng', 'uid_acc'],
      createdAt: now,
      updatedAt: now
    });
    await adminDb.collection('company_invites').doc(inviteCode).set({
      companyId,
      companyName: 'شركة البنيان للأشغال العمومية',
      ownerId: 'uid_admin',
      defaultRole: 'مهندس موقع',
      active: true,
      createdAt: now,
      updatedAt: now
    });
    await adminDb.collection('companies').doc(companyId).collection('members').doc('uid_admin').set({
      uid: 'uid_admin',
      companyId,
      fullName: 'كريم بن علي',
      email: 'admin@btp.dz',
      photoUrl: '',
      role: 'مدير عام',
      branchName: 'المقر الرئيسي',
      preferredLanguageCode: 'ar',
      inviteCode,
      active: true,
      joinedAt: now,
      updatedAt: now
    });
    await adminDb.collection('companies').doc(companyId).collection('members').doc('uid_eng').set({
      uid: 'uid_eng',
      companyId,
      fullName: 'يوسف منصوري',
      email: 'eng@btp.dz',
      photoUrl: '',
      role: 'مهندس موقع',
      branchName: 'ورشة الشرق',
      preferredLanguageCode: 'fr',
      inviteCode,
      active: true,
      joinedAt: now,
      updatedAt: now
    });
    await adminDb.collection('companies').doc(companyId).collection('members').doc('uid_acc').set({
      uid: 'uid_acc',
      companyId,
      fullName: 'سارة بوزيد',
      email: 'acc@btp.dz',
      photoUrl: '',
      role: 'محاسب مالي',
      branchName: 'المالية',
      preferredLanguageCode: 'ar',
      inviteCode,
      active: true,
      joinedAt: now,
      updatedAt: now
    });
  });
}

test('denies all unauthenticated reads and writes', async () => {
  await seedCompanyWorkspace();
  const guest = unauthDb();
  await assertFails(guest.collection('users').doc('uid_admin').get());
  await assertFails(guest.collection('companies').doc('comp_btp_1').get());
  await assertFails(guest.collection('companies').doc('comp_btp_1').collection('employees').get());
  await assertFails(guest.collection('users').doc('uid_guest').set({
    uid: 'uid_guest',
    fullName: 'Guest',
    email: '',
    photoUrl: '',
    role: 'مهندس موقع',
    branchName: 'Site',
    preferredLanguageCode: 'ar',
    activeCompanyId: '',
    active: true,
    rewardPoints: 0,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  }));
});

test('enforces strict per-user owner isolation on /users/{userId} and blocks list', async () => {
  const adminDb = dbFor('uid_admin');
  const otherDb = dbFor('uid_outsider');

  await assertSucceeds(adminDb.collection('users').doc('uid_admin').set({
    uid: 'uid_admin',
    fullName: 'كريم بن علي',
    email: 'admin@btp.dz',
    photoUrl: '',
    role: 'مدير عام',
    branchName: 'المقر الرئيسي',
    preferredLanguageCode: 'ar',
    activeCompanyId: '',
    active: true,
    rewardPoints: 120,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  }));

  await assertSucceeds(adminDb.collection('users').doc('uid_admin').get());
  await assertFails(otherDb.collection('users').doc('uid_admin').get());
  await assertFails(adminDb.collection('users').get());
  await assertFails(otherDb.collection('users').doc('uid_admin').update({
    fullName: 'Hacked',
    updatedAt: serverTimestamp()
  }));
});

test('supports invite-code joining by employee while blocking unauthorized role escalation', async () => {
  await seedCompanyWorkspace();
  const newEmpDb = dbFor('uid_new_emp');

  // Cannot list invites
  await assertFails(newEmpDb.collection('company_invites').get());
  // Can get specific invite by code
  await assertSucceeds(newEmpDb.collection('company_invites').doc('BTP7K9M2').get());

  // Cannot self-assign مدير عام when invite specifies مهندس موقع
  await assertFails(
    newEmpDb.collection('companies').doc('comp_btp_1').collection('members').doc('uid_new_emp').set({
      uid: 'uid_new_emp',
      companyId: 'comp_btp_1',
      fullName: 'عامل جديد',
      email: 'worker@btp.dz',
      photoUrl: '',
      role: 'مدير عام',
      branchName: 'الميدان',
      preferredLanguageCode: 'ar',
      inviteCode: 'BTP7K9M2',
      active: true,
      joinedAt: serverTimestamp(),
      updatedAt: serverTimestamp()
    })
  );

  // Valid join with invite's defaultRole succeeds
  await assertSucceeds(
    newEmpDb.collection('companies').doc('comp_btp_1').collection('members').doc('uid_new_emp').set({
      uid: 'uid_new_emp',
      companyId: 'comp_btp_1',
      fullName: 'عامل جديد',
      email: 'worker@btp.dz',
      photoUrl: '',
      role: 'مهندس موقع',
      branchName: 'الميدان',
      preferredLanguageCode: 'ar',
      inviteCode: 'BTP7K9M2',
      active: true,
      joinedAt: serverTimestamp(),
      updatedAt: serverTimestamp()
    })
  );

  // Appending self to memberUids on company succeeds once member doc exists
  await assertSucceeds(
    newEmpDb.collection('companies').doc('comp_btp_1').update({
      memberUids: ['uid_admin', 'uid_eng', 'uid_acc', 'uid_new_emp'],
      updatedAt: serverTimestamp()
    })
  );
});

test('synchronizes employees & attendance between admin and employee while blocking non-members', async () => {
  await seedCompanyWorkspace();
  const adminDb = dbFor('uid_admin');
  const engDb = dbFor('uid_eng');
  const outsiderDb = dbFor('uid_outsider');

  const empRef = adminDb.collection('companies').doc('comp_btp_1').collection('employees').doc('emp_doc_1');
  await assertSucceeds(empRef.set({
    fullName: 'أحمد بلقاسم',
    role: 'مراقب ورشة',
    phone: '0550112233',
    nationalId: '100200300',
    dailyWage: 3500.0,
    assignedSiteName: 'ورشة المقطع 14',
    attendanceStatus: 'حاضر',
    hireDate: '2026-01-10',
    active: true,
    companyId: 'comp_btp_1',
    updatedBy: 'uid_admin',
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  }));

  // Employee in same company can read list & update attendance
  const empSnap = await assertSucceeds(
    engDb.collection('companies').doc('comp_btp_1').collection('employees').get()
  );
  assert.equal(empSnap.size, 1);

  await assertSucceeds(
    engDb.collection('companies').doc('comp_btp_1').collection('employees').doc('emp_doc_1').update({
      attendanceStatus: 'غائب',
      updatedBy: 'uid_eng',
      updatedAt: serverTimestamp()
    })
  );

  // Outsider from another company is strictly blocked
  await assertFails(outsiderDb.collection('companies').doc('comp_btp_1').collection('employees').get());
  await assertFails(
    outsiderDb.collection('companies').doc('comp_btp_1').collection('employees').doc('emp_evil').set({
      fullName: 'Intruder',
      role: 'عامل',
      phone: '',
      nationalId: '',
      dailyWage: 100,
      assignedSiteName: '',
      attendanceStatus: 'حاضر',
      hireDate: '2026-01-01',
      active: true,
      companyId: 'comp_btp_1',
      updatedBy: 'uid_outsider',
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp()
    })
  );
});

test('enforces strict RBAC isolation on sensitive expenses and invoices collections', async () => {
  await seedCompanyWorkspace();
  const adminDb = dbFor('uid_admin');
  const accDb = dbFor('uid_acc');
  const engDb = dbFor('uid_eng');

  const expRef = adminDb.collection('companies').doc('comp_btp_1').collection('expenses').doc('exp_1');
  await assertSucceeds(expRef.set({
    title: 'شراء إسمنت مقاوم',
    category: 'مواد بناء',
    amount: 185000.0,
    date: '2026-11-20',
    projectName: 'جسر وادي الحراش',
    paymentMethod: 'تحويل بنكي',
    notes: 'دفعة أولى',
    companyId: 'comp_btp_1',
    updatedBy: 'uid_admin',
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  }));

  // Accountant and Admin can read/list expenses & invoices
  await assertSucceeds(accDb.collection('companies').doc('comp_btp_1').collection('expenses').get());

  // Site engineer is DENIED reading or writing expenses/invoices at database level
  await assertFails(engDb.collection('companies').doc('comp_btp_1').collection('expenses').get());
  await assertFails(engDb.collection('companies').doc('comp_btp_1').collection('expenses').doc('exp_1').get());
  await assertFails(
    engDb.collection('companies').doc('comp_btp_1').collection('invoices').doc('inv_evil').set({
      invoiceNumber: 'INV-999',
      type: 'فاتورة مورد',
      partyName: 'مجهول',
      projectName: '',
      totalAmount: 5000,
      paidAmount: 0,
      issueDate: '2026-11-20',
      dueDate: '2026-12-20',
      status: 'غير مدفوعة',
      companyId: 'comp_btp_1',
      updatedBy: 'uid_eng',
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp()
    })
  );
});

test('rejects invalid schemas, negative values, immutable createdAt changes, and unknown fields', async () => {
  await seedCompanyWorkspace();
  const adminDb = dbFor('uid_admin');

  // Negative budget fails
  await assertFails(
    adminDb.collection('companies').doc('comp_btp_1').collection('projects').doc('prj_bad').set({
      code: 'PRJ-1',
      name: 'مشروع غير صالح',
      clientName: 'عميل',
      location: 'الجزائر',
      budget: -500,
      spentAmount: 0,
      startDate: '2026-01-01',
      endDate: '2026-12-01',
      progressPercent: 10,
      status: 'قيد التنفيذ',
      description: '',
      companyId: 'comp_btp_1',
      updatedBy: 'uid_admin',
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp()
    })
  );

  // Valid project creation then attempt to mutate createdAt fails
  const prjRef = adminDb.collection('companies').doc('comp_btp_1').collection('projects').doc('prj_ok');
  await assertSucceeds(prjRef.set({
    code: 'PRJ-100',
    name: 'مشروع الطريق السريع',
    clientName: 'وزارة الأشغال',
    location: 'الجزائر',
    budget: 2500000,
    spentAmount: 100000,
    startDate: '2026-01-01',
    endDate: '2026-12-31',
    progressPercent: 20,
    status: 'قيد التنفيذ',
    description: 'تعبيد وتجهيز',
    companyId: 'comp_btp_1',
    updatedBy: 'uid_admin',
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  }));

  // Mutating createdAt must fail
  await assertFails(prjRef.update({
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  }));

  // Unknown extra field must fail
  await assertFails(prjRef.update({
    extraField: 'shadow',
    updatedAt: serverTimestamp()
  }));

  // Valid progress update succeeds
  await assertSucceeds(prjRef.update({
    progressPercent: 45,
    updatedBy: 'uid_admin',
    updatedAt: serverTimestamp()
  }));

  // Chat message spoofing another senderUid fails
  const engDb = dbFor('uid_eng');
  await assertFails(
    engDb.collection('companies').doc('comp_btp_1').collection('chat_messages').doc('msg_spoof').set({
      channelCloudId: 'ch_1',
      senderUid: 'uid_admin',
      senderName: 'Fake Admin',
      senderRole: 'مدير عام',
      content: 'Spoofed',
      attachmentType: 'NONE',
      attachmentName: '',
      attachmentUri: '',
      timestampMs: Date.now(),
      read: false,
      companyId: 'comp_btp_1',
      updatedBy: 'uid_eng',
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp()
    })
  );

  // Valid chat message succeeds & company filtered query passes
  await assertSucceeds(
    engDb.collection('companies').doc('comp_btp_1').collection('chat_messages').doc('msg_ok').set({
      channelCloudId: 'ch_1',
      senderUid: 'uid_eng',
      senderName: 'يوسف منصوري',
      senderRole: 'مهندس موقع',
      content: 'تم فحص الأساسات',
      attachmentType: 'NONE',
      attachmentName: '',
      attachmentUri: '',
      timestampMs: Date.now(),
      read: false,
      companyId: 'comp_btp_1',
      updatedBy: 'uid_eng',
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp()
    })
  );

  const msgSnap = await assertSucceeds(
    adminDb.collection('companies').doc('comp_btp_1').collection('chat_messages')
      .where('channelCloudId', '==', 'ch_1')
      .get()
  );
  assert.equal(msgSnap.size, 1);
});
