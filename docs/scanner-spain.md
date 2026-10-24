# Spanish package identification

## Scope and source

The initial market is Spain, as requested by the user. AEMPS documents CN-based REST lookup
and different national/product-code schemes. Supported local inputs are six-digit CN,
checksum-valid EAN-13 with prefix 847000, and checksum-valid 14-digit NTIN beginning 0847000,
including supported leading GS1 (01) encodings. Other GTIN and AI (712) variants are not guessed;
the user enters CN manually. No OCR, international catalogue, lot/expiry inference or medicine
authenticity verification is claimed.

Primary references:

- https://cima.aemps.es/cima/resources/docs/CIMA_REST_API.pdf
- https://www.aemps.gob.es/informa/la-aemps-actualiza-la-informacion-a-incluir-en-la-aplicacion-de-notificacion-de-comercializacion-de-medicamentos-y-sobre-dispositivos-de-seguridad/
- https://sede.aemps.gob.es/datos-abiertos/
- https://listadomedicamentos.aemps.gob.es/Aviso_Legal_Nomenclator.pdf

## Flow and data boundary

The camera result is reduced locally to CN. Raw lot/serial fields are not put in the ViewModel,
requests or persistence. Lookup occurs only after pressing Consult CIMA. The fixed HTTPS client
requests /cima/rest/presentacion/{CN}; validated numeric input is the only query argument.
No auth token, profile, history or photo is sent. Requests have bounded time/body size; iOS
redirects are restricted to the same HTTPS host and Android does not follow redirects.

A returned object must match the requested CN and have a nonempty name. Missing, invalid and
network-error results remain distinguishable and offer manual entry. The user must confirm the
exact package match before the editable medication form opens. Only name and source metadata
are copied; quantity/unit/frequency remain unselected. Dates, inventory and prescribed dose are
never inferred from package contents or strength. Saving requires valid manual treatment data.

The source and retrieval time are shown. Optional provenance is stored in v11 medication columns
while the verified name is retained. A manually renamed saved medicine loses that attribution.
No complete dataset is bundled/cached/shared; information has no implied AEMPS endorsement.
The inherited notice/reuse terms and release attribution audit remain documented in NOTICE.

## Native reading

Android uses the version 16.1.0 Google Code Scanner UI, created only on a camera-button tap.
It may need a Play-services module download; a device without that service can use manual CN.
The app does not request a camera permission for this delegated UI. iOS uses AVFoundation,
requests camera access in context and stops capture when dismissed. NSCameraUsageDescription
is present in the application target plists. Both support EAN-13 and Data Matrix.

Google SDK terms/metrics information is disclosed before camera use; see:
https://developers.google.com/ml-kit/vision/barcode-scanning/code-scanner
https://developers.google.com/ml-kit/terms

## Verification and limits

- Shared tests cover leading zeroes, checksums, EAN/NTIN, GS1 examples, rejected foreign/ambiguous
  input, exact CN response matching, invalid responses and propagation of cancellation.
- Test medicines are synthetic, not a bundled AEMPS dataset. The complete shared suite has
  46 passing Kotlin/JUnit checks; 11 SQLite checks cover persistence/migrations and stock.
- The native iOS camera controller and HTTP adapter compile separately with Kotlin/Native 2.4.0
  for ios_simulator_arm64. This is not a running camera, app/framework build or network test.
- A live presentation endpoint was not accessible through the research browser. Its public
  documented contract is implemented; live integration is unverified. No successful live
  catalogue retrieval is claimed.
- The new Google dependency and the full Compose/KSP Android/iOS build remain unverified here.

Manual acceptance: build both targets; test a real supported EAN and NTIN Data Matrix, leading-zero
CN, denied camera, no camera, low light/cancel, missing Google module, offline lookup, unknown CN,
invalid response and a verified name. Check that only an explicit CIMA action sends a request,
match confirmation is mandatory, no stock/dose is prefilled and manual entry remains available.
Reopen a saved sourced medication and inspect its source/date; rename it without inventory and
verify removal of attribution, and check that tracked product identity cannot be replaced.
