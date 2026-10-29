# meevo-pay-deposit

A self-contained Angular web component that renders a hosted Adyen payment form, collects card data, and emits a `paymentAccountCreated` event containing the token needed to call `/ob/Prepay`. The host page is responsible for calling `/ob/Prepay` with the emitted IDs; the component does not do this itself.

---

## Embedding

### 1. Load the bundle

```html
<script src="{api-base-url}/lib/meevo-pay-deposit.js"></script>
```

Replace `{api-base-url}` with the base URL of the ClientApp gRPC gateway (e.g. `https://booking.yourdomain.com`).

### 2. Place the element

```html
<meevo-pay-deposit
  api-base-url="https://booking.yourdomain.com"
  bearer-token="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
  tenant-id="12345"
  location-id="67890"
  recaptcha-site-key="6LeXXXXXXXXXXXXXXXXXXXXX"
  transaction-amount="25.00"
  full-billing-address="true"
></meevo-pay-deposit>

<!-- Your own styled button — the component does NOT render one by default. -->
<button id="pay-deposit" disabled>Pay deposit</button>
```

### 3. Trigger submission from your own button

The component intentionally does **not** render a styled button. Call the element's `submit()` method from your UI — it runs Adyen validation, encrypts the card, and (on success) fires `paymentAccountCreated`.

```js
const el = document.querySelector('meevo-pay-deposit');
const payBtn = document.getElementById('pay-deposit');

// Enable your button once the card form is ready and valid.
el.addEventListener('hostedPageLoaded', () => { payBtn.disabled = false; });
el.addEventListener('validityChanged', (e) => { payBtn.disabled = !e.detail; });

payBtn.addEventListener('click', () => el.submit());

el.addEventListener('paymentAccountCreated', (e) => {
  const { paymentAccountId, encryptedSecurityCode } = e.detail;
  // Names are Meevo-internal (see "Payment account id mapping" below).
  // Call your backend: POST /ob/Prepay with PaymentAccountId = paymentAccountId
  // and EncryptedSecurityCode = encryptedSecurityCode.
});

el.addEventListener('paymentFailed', (e) => {
  console.error('Payment failed', e.detail);
});
```

> A built-in unstyled button is available for quick prototyping via `show-pay-button="true"`, but production embeds should use their own button + `submit()` as above.

---

## Attributes

| Attribute              | Required | Default | Description |
|------------------------|----------|---------|-------------|
| `api-base-url`         | YES      | —       | Base URL of the ClientApp gRPC gateway. Used for all proxy calls (`/ob/HostedPageData`, `/ob/Prepay`) and to resolve the bundle path for CSP. |
| `bearer-token`         | YES      | —       | Client JWT issued by the Meevo auth flow. Forwarded as `Authorization: Bearer` on every proxy call. |
| `tenant-id`            | YES      | —       | Meevo tenant identifier. |
| `location-id`          | YES      | —       | Meevo location identifier scoped to the tenant. |
| `recaptcha-site-key`   | YES      | —       | Google reCAPTCHA **V3** site key (see reCAPTCHA section below). |
| `transaction-amount`   | NO       | —       | Pre-populate the transaction amount (string, decimal). When omitted the component calls `HostedPageData` without an amount. |
| `full-billing-address` | NO       | `true`  | When `true`, renders a full billing-address form (street, city, state, ZIP). Set to `false` to collect ZIP only. |
| `billing-country-code` | NO       | —       | ISO 3166-1 alpha-2 country code pre-selected in the billing address form (e.g. `US`). |
| `show-pay-button`      | NO       | `false` | When `true`, renders a built-in **unstyled** Pay button (prototyping only). Leave unset/`false` for production and drive submission from your own button via the element's `submit()` method (see step 3). |

---

## Events

All events are DOM `CustomEvent`s dispatched on the element. The payload is in `event.detail`.

| Event                  | `detail` shape | Description |
|------------------------|----------------|-------------|
| `paymentAccountCreated` | `{ paymentAccountId, externalPaymentAccountId, processingStatus, creditCardLastFour, cardType, creditCardExpirationDate, encryptedSecurityCode, brand }` (all `string`). Field names are **Meevo-internal** (re-mapped from the Adyen-named backing response — see "Payment account id mapping"). Other non-id fields pass through unchanged. | Fired on successful hosted-page submission. Pass `paymentAccountId` and `encryptedSecurityCode` to your backend's `/ob/Prepay` call. |
| `paymentFailed`        | `{ errorCode: string, message: string, httpStatus?: number }` | Fired when an unrecoverable error occurs. See error codes below. |
| `hostedPageLoaded`     | `boolean` — `true` when the Adyen hosted-page iframe has loaded and is ready for input. | |
| `validityChanged`      | `boolean` — `true` when all required fields are valid. | |
| `cardMetadataChanged`  | `{ cardType: string, isValid: boolean }` — emitted as the user fills the card number. | |

### `paymentFailed.errorCode` values

| Code | Meaning |
|------|---------|
| `config` | One or more required attributes are missing or invalid at init time. |
| `auth` | The `bearer-token` was rejected by the gateway (401/403). |
| `hosted-page-init` | The call to `/ob/HostedPageData` failed or returned an unusable response. |
| `unsupported-provider` | The merchant's payment provider is not Adyen. |
| `sdk-load` | The Adyen Web SDK script could not be loaded. |
| `card-declined` | Adyen returned a decline result for the submitted card. |
| `hosted-page-update` | The call to update the hosted page (before final authorisation) failed. |

---

## reCAPTCHA V3

`recaptcha-site-key` must be a Google **reCAPTCHA V3** site key registered for the **action `HostedPage`** and associated with the same domain that hosts the embedding page.

The component loads `https://www.google.com/recaptcha/api.js?render={recaptcha-site-key}` and executes the `HostedPage` action to obtain a token. That token is forwarded as `RcToken` in the request body to `/ob/HostedPageData`. The backing OnlineBooking API validates the token server-side.

If reCAPTCHA is unavailable (network error or script failure), the component emits `paymentFailed` with `errorCode: 'hosted-page-init'` and does **not** call `/ob/HostedPageData`.

---

## CSP checklist

The page embedding the component must add the following directives to its `Content-Security-Policy` response header. Replace `{api-base-url}` and `{radar-base-url}` with the actual origins.

```
script-src  {api-base-url} https://*.adyen.com https://www.google.com https://www.gstatic.com;
connect-src {api-base-url} https://*.adyen.com {radar-base-url};
frame-src   https://*.adyen.com https://www.google.com;
style-src   https://*.adyen.com 'unsafe-inline';
```

**Why each directive:**

- `script-src {api-base-url}` — the bundle itself is loaded from the gateway.
- `script-src https://*.adyen.com` — Adyen Web SDK scripts injected at runtime.
- `script-src https://www.google.com https://www.gstatic.com` — reCAPTCHA V3 scripts.
- `connect-src {api-base-url}` — XHR/fetch to `/ob/HostedPageData` and related proxy endpoints.
- `connect-src https://*.adyen.com` — Adyen iframe posts and tokenisation calls.
- `connect-src {radar-base-url}` — Radar.io geolocation fetch; the base URL is returned by `HostedPageData` and varies by environment.
- `frame-src https://*.adyen.com` — Adyen renders card fields inside a cross-origin iframe.
- `frame-src https://www.google.com` — reCAPTCHA V3 badge iframe.
- `style-src https://*.adyen.com 'unsafe-inline'` — Adyen injects inline styles into the hosted-page iframe; `unsafe-inline` is required for correct rendering.

---

## Payment account id mapping

The backing `/ob/hostedpageupdate` response uses Adyen/external naming that is **inverted** relative to Meevo's internal naming. To prevent passing the wrong id to `/ob/Prepay`, the `paymentAccountCreated` event re-maps the two id fields to Meevo-internal names:

| `paymentAccountCreated` field | ← `/ob/hostedpageupdate` field | Meevo DB column | Use |
|---|---|---|---|
| `paymentAccountId` | `paymentAccountReferenceNumber` | `PaymentAccountId` | **Pass to `/ob/Prepay` as `PaymentAccountId`.** Guaranteed equivalent. |
| `externalPaymentAccountId` | `paymentAccountId` | `ExternalPaymentAccountId` | Reference/reconciliation only — **not** the prepay id. |

The raw `paymentAccountReferenceNumber` key is intentionally **not** present on the event detail; its value is surfaced as `paymentAccountId`. Everything else in the update response passes through unchanged.

## Prepay note

The component does **not** call `/ob/Prepay`. After receiving the `paymentAccountCreated` event, the consuming application calls `/ob/Prepay` with `PaymentAccountId = event.detail.paymentAccountId` and `EncryptedSecurityCode = event.detail.encryptedSecurityCode` to complete the deposit transaction.
