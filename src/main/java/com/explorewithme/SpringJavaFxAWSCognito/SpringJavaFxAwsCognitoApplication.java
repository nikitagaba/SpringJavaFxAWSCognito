package com.explorewithme.SpringJavaFxAWSCognito;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@SpringBootApplication
public class SpringJavaFxAwsCognitoApplication extends Application {

	private ConfigurableApplicationContext context;
	private Parent rootNode;

	public static void main(String[] args) {
		
		launch(SpringJavaFxAwsCognitoApplication.class,args);
		
//		SpringApplication.run(SpringJavaFxAwsCognitoApplication.class, args);
	}

	@Override
	public void init() throws Exception {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(SpringJavaFxAwsCognitoApplication.class);
		context = builder.run(getParameters().getRaw().toArray(new String[0]));
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Main.fxml"));
		loader.setControllerFactory(context::getBean);
		rootNode = loader.load();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
//		Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
//		double width = visualBounds.getWidth();
//		double height = visualBounds.getHeight();
		primaryStage.setScene(new Scene(rootNode, 700, 700));
		primaryStage.setTitle("Cognito Temporary Credentials");
		primaryStage.centerOnScreen();
		primaryStage.show();

	}

	@Override
	public void stop() throws Exception {
		context.close();
	}
}
