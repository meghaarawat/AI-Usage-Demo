# POST /ob/HostedPageData — Get Adyen Hosted Page Data

Retrieves the Adyen-hosted payment page configuration and token for a deposit or membership payment. The WC uses the returned data to initialize the Adyen Drop-In component. Backed by the OnlineBooking `api/ob/HostedPageData` endpoint, which requires a client JWT.

## Auth

Client session JWT (`Authorization: Bearer <jwt>`).

## Route

```
POST /ob/HostedPageData
```

## Request

`Content-Type: application/json`

| Field | Type | Required | Description |
|---|---|---|---|
| `ClientVersion` | string | no | Adyen client-side SDK version to request |
| `RcToken` | string | **yes** | Google reCAPTCHA **V3** token. The backing endpoint enforces reCAPTCHA (it does **not** fail-open for this route) and verifies the token for action `HostedPage` with a minimum score. Obtain it client-side with `grecaptcha.execute(siteKey, { action: 'HostedPage' })`. |
| `TransactionAmount` | decimal | no | Deposit amount; drives surcharge calculation |
| `IsMembershipRequest` | bool | no | `true` when the hosted page is for a membership payment |
| `MerchantAccountId` | Guid | no | Override the tenant default merchant account |
| `ClientId` | Guid | no | Client id; used to pre-populate card-on-file options |
| `CustomCss` | string | no | Custom CSS injected into the hosted page |
| `SubmitButtonText` | string | no | Label for the hosted page submit button |

**reCAPTCHA:** This route is the one deposit proxy that forwards `RcToken`. The backing `GetHostedPageDataRequest` defaults `RcVersion` to V3 (matching the backing filter's configured version), so an absent/invalid token is rejected with `412 [missing-input-response]` / `[reCaptchaResult failed: invalid-input-response]`. `RcVersion` is omitted from this proxy DTO on purpose — the backing default (V3) is used. The other deposit proxies (`/ob/hostedpageupdate`, `/ob/Prepay`) do not carry a reCAPTCHA filter.

### Example request

```json
{
  "RcToken": "03AGdBq27...valid-v3-token...",
  "TransactionAmount": 50.00,
  "IsMembershipRequest": false,
  "ClientId": "a1b2c3d4-0000-0000-0000-000000000000"
}
```

## Response

`Content-Type: application/json`

Adyen-shaped response from the backing OnlineBooking service. Contains the Adyen `paymentMethodsResponse`, `clientKey`, `environment`, and session details required to initialize the Drop-In component.

### Example response

```json
{
  "clientKey": "live_xxxxxxxxxxxx",
  "environment": "live",
  "paymentMethodsResponse": { ... },
  "sessionId": "CS...",
  "sessionData": "..."
}
```
