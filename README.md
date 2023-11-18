# cnat-api-gateway
The API Gateway microservice is the facade of the application and exposes only the necessary 
endpoint to the internet. It issues JWTs for users and trackers and handles authorization for all 
exposed endpoints so that other microservices do not need to have their own authorization 
layers. This component also performs basic validation such as the existence of required fields to 
prevent unnecessary load on internal services.

## Software Architecture:
The software architecture in this microservice consists of two layers; The controller layer and 
the service layer. The REST API responses are defined in response objects. Response objects help 
the client understand the response format it will receive when sending a request to each 
endpoint.
In the design of this application, the controller layer is responsible for creating the URL mapping 
with the required method, response type, and body or query parameter. The constraints are also 
defined for each field of the request objects to allow the controller to perform validation checks 
before processing the request. This layer also performs the checks on the JWT’s role attribute
to authorize access to the endpoint. More details about roles and JWTs are described in the
following sections.

The service layer is an interface representing the endpoints of each of the microservices. For 
instance, two service interfaces called `TrackerService` and `UserService` exist for the API Gateway.
These services perform the corresponding API calls or send event messages to the related topic
and handle any unexpected errors during the process. 
In case of an error, a new exception of the appropriate type is created and thrown to pass down 
the information to the client. Multiple exceptions are defined to reflect different types of errors 
that might happen while serving a request.

## Authentication:
Due to microservices being ephemeral, stateless authentication is chosen over session-based 
authentication as no data needs to be recorded in this method. This approach is also suitable 
for horizontal scaling of the API gateway microservice. Secured paths require a valid JWT passed 
in the Authorization HTTP request header in the form of `Bearer <JWT>`. 
The API gateway generates signed JWTs for a user when it authenticates using its username and 
password and for a tracker only once upon its registration. As user tokens are meant to be used 
by the Web App running in a web browser, an expiry date is set to improve security. However, 
trackers will carry the same token permanently, therefore no expiry date is specified in their 
token. The JWTs are signed using the API Gateway’s private asymmetric key to give them 
authenticity. When the API Gateway receives the authorization header, it verifies the 
authenticity of the JWT by verifying its signature with its own pair of keys. After the signature
is proved authentic, the *nbf* (Not Before) and *exp* (Expiration Time) claims are verified to be valid.

## Authorization:
An API endpoint might need to restrict access to a specific client role, user or tracker. For this 
purpose, a *role* key is added to the JWT claims when issuing the token. Subsequently, API 
endpoints can verify if a request has the required role.
The sub (Subject) claim indicates the email in a user JWT, or the ID in a tracker JWT. Example 
JWT claims for a user:
`{
sub: "johndoe@example.com",
role: "User",
nbf: 1672531200,
exp: 1672617600
}`
Example JWT claims for a tracker:
`{
sub: "507f191e810c19729de860ea",
role: "Tracker",
nbf: 1672531200
}`
In some cases, such as fetching a tracker or tracker data, validating the role is not enough; the 
subject of the user must match the owner of the tracker it is trying to fetch. Therefore, the API 
Gateway also verifies ownership before returning the data.

## API:
| Method | Path                  | Secured | Request                                      | Response                     | Description                                      |
|--------|-----------------------|---------|----------------------------------------------|------------------------------|--------------------------------------------------|
| POST   | /trackers             | ✅       | Body: TrackerRegisterRequest                 | TrackerRegisterResponse      | Register a new tracker                           |
| GET    | /trackers             | ✅       |                                              | TrackersGetResponse          | Get all user’s trackers                          |
| GET    | /trackers/{id}        | ✅       |                                              | TrackerGetResponse           | Get tracker                                      |
| DELETE | /trackers/{id}        | ✅       |                                              | Status code                  | Delete tracker                                   |
| GET    | /trackers/{id}/data   | ✅       | Query param: from, to, hasCoordinates, limit | TrackerDataGetResponse       | Get tracker’s data                               |
| GET    | /trackers/data/latest | ✅       |                                              | LatestTrackerDataGetResponse | Get latest data form each of the user’s trackers |
| POST   | /trackers/data        | ✅       | Body: TrackerDataRegisterRequest             | Status code                  | Register a new tracker data                      |
| POST   | /users                | ❌       | Body: UserRegisterRequest                    | Status code                  | Register a new user                              |
| POST   | /users/auth           | ❌       | Body: UserAuthRequest                        | Status code                  | Authenticate a user                              |
| DELETE | /users                | ✅       | Body: UserDeleteRequest                      | Status code                  | Delete a user                                    |

## Deployment

Using Docker:
```bash
docker build -t cnat-api-gateway .
docker run --name some-cnat-api-gateway -dp 80:80 \
  -e CNAT_KAFKA_URI=your_kafka_uri \
  -e CNAT_API_GATEWAY_KEYSTORE_PASSWORD=your_keystore_password \
  -e CNAT_API_GATEWAY_JWT_KEY_ALIAS=your_jwt_key_alias \
  -e CNAT_API_GATEWAY_JWT_KEY_PASSWORD=your_jwt_key_password \
  -e CNAT_KAFKA_TRACKER_DATA_TOPIC=your_tracker_data_topic \
  -e CNAT_TRACKER_SERVICE_URI=your_tracker_service_uri \
  -e CNAT_USER_SERVICE_URI=your_user_service_uri \
  cnat-api-gateway
```

Using Maven:
```bash
mvn clean package
java -jar \
  -DCNAT_KAFKA_URI=your_kafka_uri \
  -DCNAT_API_GATEWAY_KEYSTORE_PASSWORD=your_keystore_password \
  -DCNAT_API_GATEWAY_JWT_KEY_ALIAS=your_jwt_key_alias \
  -DCNAT_API_GATEWAY_JWT_KEY_PASSWORD=your_jwt_key_password \
  -DCNAT_KAFKA_TRACKER_DATA_TOPIC=your_tracker_data_topic \
  -DCNAT_TRACKER_SERVICE_URI=your_tracker_service_uri \
  -DCNAT_USER_SERVICE_URI=your_user_service_uri \
  target/cnat-api-gateway-0.0.1-SNAPSHOT.jar
```
