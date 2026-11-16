# ValeERP Cliente (Kotlin) — `ERP-customer-mobile`

App de **cliente** (tesis). Primera funcionalidad: **seguir un despacho** por código `DSP-…` haciendo **polling** a `GET /api/dispatch/trips/{code}`.

## Por qué polling

Para no gastar RAM/CPU en VMs universitarias: sin FCM, sin WebSocket dedicado, sin broker. El store-service que ya corre es suficiente.

## Uso

1. Abre en Android Studio esta carpeta.
2. En Ops crea un despacho y copia el código.
3. Pégalo aquí y pulsa **Seguir**.
4. API emulador: `http://10.0.2.2:8083/api`.
