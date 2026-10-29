# POST /ob/hostedpageupdate — Submit Hosted Page Payment Result

Submits the tokenized card data produced by the Adyen Drop-In component after the client completes entry on the hosted page. Returns the updated payment account reference. Backed by the OnlineBooking `api/ob/hostedpageupdate` endpoint, which requires a client JWT.

## Auth

Client session JWT (`Authorization: Bearer <jwt>`).

## Route

```
POST /ob/hostedpageupdate
```

## Request

`Content-Type: application/json`

| Field | Type | Required | Description |
|---|---|---|---|
| `CardHolderName` | string | no | Cardholder name as entered on the hosted page |
| `SetupId` | string | yes | Adyen setup/session id returned by the Drop-In |
| `PaymentAccountReferenceNumber` | string | no | Payment account reference number from Adyen |
| `PaymentAccountRequestId` | Guid | no | Internal payment account request tracking id |
| `PostalCode` | string | no | Billing postal code entered on the hosted page |
| `SetupData` | string | no | Opaque setup data blob from the Adyen Drop-In |
| `IsCardOnFile` | bool | no | `true` to retain the card for future charges |

### Example request

```json
{
  "SetupId": "CS_abc123",
  "CardHolderName": "Jane Smith",
  "PostalCode": "07024",
  "IsCardOnFile": false
}
```

## Response

`Content-Type: application/json`

Backed response from the OnlineBooking service. Contains the resolved `PaymentAccountId` (Guid) and reference number used in the subsequent `/ob/Prepay` call.

### Example response

```json
{
  "PaymentAccountId": "b2c3d4e5-0000-0000-0000-000000000000",
  "PaymentAccountReferenceNumber": "ADYEN_REF_XXXX"
}
```
