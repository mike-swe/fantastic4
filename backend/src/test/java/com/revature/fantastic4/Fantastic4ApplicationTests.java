package com.revature.fantastic4;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

@SpringBootTest
class Fantastic4ApplicationTests {

	public static void main(String[] args)
	{
		WebDriver driver = null;
		try {
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--start-maximized");
			driver = new ChromeDriver(options);
		} finally {
			if (driver != null) {
				driver.quit();
			}
		}
	}

	@Test
	void contextLoads() {
	}

}
