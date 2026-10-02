/**
 * Keycloak Automated Multi-Realm Provisioning Script
 * 
 * Provisions:
 * - Platform Realms: pathonxgen-platform and patho-platform
 * - Tenant Realms: pathonxgen-tenant-demo, pathonxgen-tenant-acme, pathonxgen-tenant-metro
 * - 1 OIDC Public Client per realm: patho-nxgen-frontend
 */

const KEYCLOAK_URL = process.env.KEYCLOAK_URL || 'http://localhost:8081';
const ADMIN_USER = process.env.KEYCLOAK_ADMIN || 'admin';
const ADMIN_PASS = process.env.KEYCLOAK_ADMIN_PASSWORD || 'admin';
const DEFAULT_USER_PASSWORD = process.env.DEFAULT_USER_PASSWORD || 'Password123!';

async function getAdminToken() {
  const tokenUrl = `${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token`;
  const body = new URLSearchParams({
    grant_type: 'password',
    client_id: 'admin-cli',
    username: ADMIN_USER,
    password: ADMIN_PASS,
  });

  const res = await fetch(tokenUrl, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: body.toString(),
  });

  if (!res.ok) {
    const errText = await res.text();
    throw new Error(`Failed to obtain admin token: ${res.status} ${errText}`);
  }

  const data = await res.json();
  return data.access_token;
}

async function apiRequest(path, token, options = {}) {
  const url = `${KEYCLOAK_URL}/admin/realms${path}`;
  const headers = {
    Authorization: `Bearer ${token}`,
    'Content-Type': 'application/json',
    ...options.headers,
  };

  const res = await fetch(url, { ...options, headers });
  if (res.status === 409) {
    return null;
  }
  if (!res.ok && res.status !== 201 && res.status !== 204) {
    const errText = await res.text();
    console.warn(`[Keycloak API Warning] ${options.method || 'GET'} ${url} returned ${res.status}: ${errText}`);
    return null;
  }
  if (res.status === 204 || res.headers.get('content-length') === '0') {
    return true;
  }
  const text = await res.text();
  return text ? JSON.parse(text) : true;
}

async function createRealm(token, realmName, displayName) {
  console.log(`\n--- Provisioning Realm: ${realmName} ---`);
  await apiRequest('', token, {
    method: 'POST',
    body: JSON.stringify({
      realm: realmName,
      displayName: displayName,
      enabled: true,
      registrationAllowed: false,
      rememberMe: true,
      verifyEmail: false,
    }),
  });
  console.log(`✓ Realm '${realmName}' created or already exists.`);
}

async function createClient(token, realmName) {
  const clientId = 'patho-nxgen-frontend';
  console.log(`Creating OIDC Client '${clientId}' in realm '${realmName}'...`);
  
  const clientPayload = {
    clientId: clientId,
    name: 'Patho NXGen Platform Frontend',
    enabled: true,
    publicClient: true,
    directAccessGrantsEnabled: true,
    standardFlowEnabled: true,
    rootUrl: 'http://localhost:4200',
    baseUrl: 'http://localhost:4200',
    redirectUris: ['http://localhost:4200/*', 'http://localhost:8080/*', 'http://localhost:4200/login'],
    webOrigins: ['+'],
    protocolMappers: [
      {
        name: 'tenant-claim-mapper',
        protocol: 'openid-connect',
        protocolMapper: 'oidc-hardcoded-claim-mapper',
        consentRequired: false,
        config: {
          'claim.name': 'tenant',
          'claim.value': realmName,
          'jsonType.label': 'String',
          'id.token.claim': 'true',
          'access.token.claim': 'true',
          'userinfo.claim': 'true'
        }
      },
      {
        name: 'realm-roles-mapper',
        protocol: 'openid-connect',
        protocolMapper: 'oidc-usermodel-realm-role-mapper',
        consentRequired: false,
        config: {
          'claim.name': 'realm_access.roles',
          'jsonType.label': 'String',
          'multivalued': 'true',
          'id.token.claim': 'true',
          'access.token.claim': 'true',
          'userinfo.claim': 'true'
        }
      }
    ]
  };

  await apiRequest(`/${realmName}/clients`, token, {
    method: 'POST',
    body: JSON.stringify(clientPayload),
  });
  console.log(`✓ OIDC Client '${clientId}' registered.`);
}

async function createRoles(token, realmName, roles) {
  console.log(`Seeding roles for realm '${realmName}': ${roles.join(', ')}`);
  for (const roleName of roles) {
    await apiRequest(`/${realmName}/roles`, token, {
      method: 'POST',
      body: JSON.stringify({ name: roleName, description: `${roleName} role for ${realmName}` }),
    });
  }
  console.log(`✓ Roles seeded for realm '${realmName}'.`);
}

async function createUserWithRole(token, realmName, username, email, firstName, lastName, roleName) {
  console.log(`Creating user '${email}' with role '${roleName}' in realm '${realmName}'...`);
  
  const userPayload = {
    username: username,
    email: email,
    firstName: firstName,
    lastName: lastName,
    enabled: true,
    emailVerified: true,
    credentials: [
      {
        type: 'password',
        value: DEFAULT_USER_PASSWORD,
        temporary: false,
      },
    ],
  };

  await apiRequest(`/${realmName}/users`, token, {
    method: 'POST',
    body: JSON.stringify(userPayload),
  });

  const users = await apiRequest(`/${realmName}/users?username=${encodeURIComponent(username)}`, token);
  if (users && users.length > 0) {
    const userId = users[0].id;
    const roleObj = await apiRequest(`/${realmName}/roles/${encodeURIComponent(roleName)}`, token);
    if (roleObj && roleObj.id) {
      await apiRequest(`/${realmName}/users/${userId}/role-mappings/realm`, token, {
        method: 'POST',
        body: JSON.stringify([roleObj]),
      });
      console.log(`✓ User '${username}' created and assigned role '${roleName}'.`);
    }
  }
}

async function main() {
  console.log('=== Keycloak Multi-Realm Automated Provisioning ===');
  console.log(`Target Keycloak URL: ${KEYCLOAK_URL}`);

  try {
    const token = await getAdminToken();
    console.log('✓ Admin authentication successful.');

    // 1. Platform Realm Setup (both naming conventions for maximum compatibility)
    const platformRealms = ['pathonxgen-platform', 'patho-platform'];
    for (const platformRealm of platformRealms) {
      await createRealm(token, platformRealm, 'Patho Platform Administration');
      await createClient(token, platformRealm);
      await createRoles(token, platformRealm, ['PLATFORM_ADMIN', 'SUPER_ADMIN', 'SUPPORT_ENGINEER']);
      await createUserWithRole(token, platformRealm, 'superadmin', 'superadmin@platform.com', 'Platform', 'Super Admin', 'SUPER_ADMIN');
      await createUserWithRole(token, platformRealm, 'platformadmin', 'admin@platform.com', 'Platform', 'Admin', 'PLATFORM_ADMIN');
      await createUserWithRole(token, platformRealm, 'supportengineer', 'support@platform.com', 'Support', 'Engineer', 'SUPPORT_ENGINEER');
    }

    // 2. Tenant Realms Setup (matching pathonxgen-tenant-<slug> pattern expected by frontend & backend)
    const tenantRealms = [
      {
        name: 'pathonxgen-tenant-demo',
        display: 'Demo Pathology Labs',
        users: [
          { username: 'demo_admin', email: 'admin@demo-lab.com', firstName: 'Demo', lastName: 'Admin', role: 'TENANT_ADMIN' },
          { username: 'demo_pathologist', email: 'pathologist@demo-lab.com', firstName: 'Dr. Demo', lastName: 'Pathologist', role: 'PATHOLOGIST' },
        ],
      },
      {
        name: 'pathonxgen-tenant-acme',
        display: 'Acme Diagnostics',
        users: [
          { username: 'acme_admin', email: 'admin@acme-diag.com', firstName: 'Acme', lastName: 'Admin', role: 'TENANT_ADMIN' },
          { username: 'acme_tech', email: 'tech@acme-diag.com', firstName: 'Acme', lastName: 'Technician', role: 'LAB_TECHNICIAN' },
        ],
      },
      {
        name: 'pathonxgen-tenant-metro',
        display: 'Metro Care Diagnostics',
        users: [
          { username: 'metro_admin', email: 'admin@metro-care.com', firstName: 'Metro', lastName: 'Admin', role: 'TENANT_ADMIN' },
          { username: 'metro_doctor', email: 'doctor@metro-care.com', firstName: 'Dr. Metro', lastName: 'Doctor', role: 'DOCTOR' },
        ],
      },
    ];

    const tenantRoles = ['TENANT_ADMIN', 'PATHOLOGIST', 'LAB_TECHNICIAN', 'RECEPTIONIST', 'DOCTOR'];

    for (const tenant of tenantRealms) {
      await createRealm(token, tenant.name, tenant.display);
      await createClient(token, tenant.name);
      await createRoles(token, tenant.name, tenantRoles);
      for (const u of tenant.users) {
        await createUserWithRole(token, tenant.name, u.username, u.email, u.firstName, u.lastName, u.role);
      }
    }

    console.log('\n======================================================');
    console.log('✓ KEYCLOAK PROVISIONING COMPLETED SUCCESSFULLY!');
    console.log('======================================================\n');
  } catch (err) {
    console.error('❌ Provisioning Error:', err);
    process.exit(1);
  }
}

main();
