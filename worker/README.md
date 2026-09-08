# Al-Daftar Support Worker

This Cloudflare Worker provides the application's Support ID service only.

## Endpoints

- `GET /health`
- `GET /v1/support/identity` — authenticated with Firebase Auth + Firebase App Check.
- `GET /v1/admin/support-identities/{supportId}` — administrator lookup.

The former entitlement, activation, device-license, lease, and trial service has been removed from this project.
