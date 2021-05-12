# Job Hunter Payment Service

## Environment Variables

- AUTH_VERIFICATION_URL
- WEB_HOOK_KEY
- STRIPE_API_KEY

#### AUTH_VERIFICATION_URL

`AUTH_VERIFICATION_URL` indicates the url to the Authentication Service

This Environment Variable is used to access the Authentication Service in order authorize certain requests

Example:
> AUTH_VERIFICATION_URL=http://localhost:8090/api/auth/validateId

## Endpoints

### Checkout

| URL | API Gateway URL | Method |
| ------ | ------ | ------ |
| /checkout | /api/payment/activeJobs | POST |

#### Notes

> Requires Authorization Header with JWT

#### Description

Create a Session that connects to the Stripe API

> This Endpoint should be used when the Employer wants to make payment

#### Request

Request Body: PaymentDTO

```json
{
  "jobId": "",
  "jobName": "",
  "employerId": "",
  "freelancerId": "",
  "amount": 0.0,
  "successUrl": "",
  "cancelUrl": ""
}
```

#### Response

The Response consists of a Checkout Session ID

### Get Employer Payments

| URL | API Gateway URL | Method |
| ------ | ------ | ------ |
| /getPayments/{employerId} | /api/payment/getPayments/{employerId} | GET |

#### Notes

> Requires Authorization Header with JWT

#### Description

Get All the Payments of an Employer using the `employerId`

#### Request

PathVariable: employerId

#### Response

It will return a list of JobOfferPayment

JobOfferPayment

```json
{
  "stripeId": "",
  "status": "",
  "amount": 0.0,
  "jobId": "",
  "jobName": "",
  "employerId": "",
  "freelancerId": ""
}
```

### Get Customer

| URL | API Gateway URL | Method |
| ------ | ------ | ------ |
| /getCustomer/{userId} | /api/payment/getCustomer/{userId} | GET |

#### Notes

> Requires Authorization Header with JWT

#### Description

Get info of a Customer using the `userId`

#### Request

PathVariable: employerId

#### Response

It will return a JobHunterCustomer

```json
{
  "userId": "",
  "stripeId": "",
  "customerType": "",
  "name": "",
  "email": "",
  "phoneNumber": "",
  "location": "",
  "payments": []
}
```

```java
public enum CustomerType {
    EMPLOYER,
    FREELANCER
}
```

### Get Customer by Stripe ID

| URL | API Gateway URL | Method |
| ------ | ------ | ------ |
| /getCustomerByStripeId/{stripeId} | /api/payment/getCustomerByStripeId/{stripeId} | GET |

#### Notes

> Requires Authorization Header with JWT

#### Description

Get info of a Customer using its `stripeId`

#### Request

PathVariable: stripeId

#### Response

It will return a JobHunterCustomer

```json
{
  "userId": "",
  "stripeId": "",
  "customerType": "",
  "name": "",
  "email": "",
  "phoneNumber": "",
  "location": "",
  "payments": []
}
```

```java
public enum CustomerType {
    EMPLOYER,
    FREELANCER
}
```

### Create Customer

| URL | API Gateway URL | Method |
| ------ | ------ | ------ |
| /createCustomer | /api/payment/createCustomer | POST |

#### Notes

> Requires Authorization Header with JWT

#### Description

Used to create a Customer manually. Normally, the customer should be created automatically when a new account is
created.

#### Request

RequestBody: CreateCustomerDTO

```json
{
  "userId": "",
  "customerType": "",
  "name": "",
  "email": "",
  "phoneNumber": "",
  "location": ""
}
```

```java
public enum CustomerType {
    EMPLOYER,
    FREELANCER
}
```

#### Response

It will return a JobHunterCustomer

```json
{
  "userId": "",
  "stripeId": "",
  "customerType": "",
  "name": "",
  "email": "",
  "phoneNumber": "",
  "location": "",
  "payments": []
}
```

```java
public enum CustomerType {
    EMPLOYER,
    FREELANCER
}
```

### Update Customer

| URL | API Gateway URL | Method |
| ------ | ------ | ------ |
| /updateCustomer | /api/payment/updateCustomer | POST |

#### Notes

> Requires Authorization Header with JWT

#### Description

Used to update a Customer manually. Normally, the customer should be updated automatically when the profile is updated.

#### Request

RequestBody: UpdateCustomerDTO

```json

{
  "userId": "",
  "email": "",
  "name": "",
  "phoneNumber": "",
  "location": ""
}
```

#### Response

It will return a JobHunterCustomer

```json
{
  "userId": "",
  "stripeId": "",
  "customerType": "",
  "name": "",
  "email": "",
  "phoneNumber": "",
  "location": "",
  "payments": []
}
```

```java
public enum CustomerType {
    EMPLOYER,
    FREELANCER
}
```