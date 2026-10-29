# POST /ob/Prepay — Charge Deposit

Charges the deposit for an appointment booking using the `PaymentAccountId` produced by `/ob/hostedpageupdate`. Called by the **consuming app** (not the WC) once it has assembled the full booking request and the payment account id that the WC emitted. Backed by the OnlineBooking `api/ob/Prepay` endpoint, which requires a client JWT.

## Auth

Client session JWT (`Authorization: Bearer <jwt>`).

## Route

```
POST /ob/Prepay
```

## Request

`Content-Type: application/json`

| Field | Type | Required | Description |
|---|---|---|---|
| `BookRequest` | object | yes | Full appointment booking request (see `/ob/Appointment` DTO fields) |
| `PaymentAccountId` | Guid | yes | Meevo-internal payment account id. Use the value the web component emits as **`event.detail.paymentAccountId`** on `paymentAccountCreated` (the component re-maps the backing's inverted naming: that value is the update response's `paymentAccountReferenceNumber`, i.e. the Meevo `PaymentAccountId` column — NOT the external/Adyen `paymentAccountId`). |
| `ClientGlobalId` | Guid | yes | Global client id for the booking client |
| `RetainCardOnFile` | bool | no | Whether to keep the card on file after the deposit charge |
| `EncryptedSecurityCode` | string | no | Encrypted CVV from the web component's `event.detail.encryptedSecurityCode` |
| `DocsAndFormUrl` | string | no | Docs-and-forms base URL appended to confirmation communications |

**Note:** `ClientIpAddress` and `DeviceFingerprint` on the backing `ObPrepayRequest` are populated server-side; they are not accepted from the client and are omitted from the gateway DTO.

### Example request

```json
{
  "BookRequest": {
    "ClientId": "a1b2c3d4-0000-0000-0000-000000000000",
    "LocationId": 1001,
    "AppointmentDate": "2026-10-15T00:00:00",
    "Services": [ { ... } ]
  },
  "PaymentAccountId": "b2c3d4e5-0000-0000-0000-000000000000",
  "ClientGlobalId": "c3d4e5f6-0000-0000-0000-000000000000",
  "RetainCardOnFile": false
}
```

## Response

`Content-Type: application/json`

Backed response from the OnlineBooking service. Contains the booked appointment details and deposit charge confirmation.
