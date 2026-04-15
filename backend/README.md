# Smart Pet Backend

Backend em Java 17 com Spring Boot 3.

## Rodar

```bash
mvn spring-boot:run
```

## Endpoints

- `GET /api/dashboard`
- `GET /api/products`
- `POST /api/products`
- `POST /api/products/{id}/stock/in`
- `POST /api/products/{id}/stock/out`
- `GET /api/customers`
- `POST /api/customers`
- `GET /api/sales`
- `POST /api/sales`
- `PATCH /api/sales/{id}/cancel`

## Banco

- H2 em arquivo local
- Console: `http://localhost:8080/h2-console`
