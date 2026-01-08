@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AdminLoginTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
    }

    @BeforeEach
    public void loginSetup() {
        RestAssured.basePath = "/login";
    }

    @Test
    public void adminLoginPositiveTest() {
        User credentials = new User();
        credentials.setUsername("admin");
        credentials.setPassword("admin");
        given()
                    .contentType(ContentType.JSON)
                    .body(credentials)
                    .header("Authorization", "token goes here")
        .when())
                    .post("/admin")
		.then()
                    .statusCode(200)
                    .body("token", notNullValue())
        }

    }