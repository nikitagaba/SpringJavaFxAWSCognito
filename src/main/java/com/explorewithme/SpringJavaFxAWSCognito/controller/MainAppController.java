package com.explorewithme.SpringJavaFxAWSCognito.controller;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentityClient;
import com.amazonaws.services.cognitoidentity.model.GetCredentialsForIdentityRequest;
import com.amazonaws.services.cognitoidentity.model.GetCredentialsForIdentityResult;
import com.amazonaws.services.cognitoidentity.model.GetOpenIdTokenForDeveloperIdentityRequest;
import com.amazonaws.services.cognitoidentity.model.GetOpenIdTokenForDeveloperIdentityResult;
import com.explorewithme.SpringJavaFxAWSCognito.SpringJavaFxAwsCognitoApplication;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

@Component
public class MainAppController implements Initializable {

  @FXML
  private Label lblAwsAccessKeyId;

  @FXML
  private Label lblUsername;

  @FXML
  private TextField txtAwsAccessKeyId;

  @FXML
  private TextField txtAwsSecretAccessKey;
  @FXML
  private TextField txtIdentityPoolId;
  @FXML
  private TextField txtDeveloperProviderNameId;
  @FXML
  private TextField txtUsername;

  @FXML
  private Button btnGenerateToken;

  @FXML
  private Button btnReset;

  @FXML
  private Button btnSendHttpRequest;

  @FXML
  private Button btnGetCred;

  @FXML
  private ChoiceBox<String> ddAwsRegion;

  @FXML
  private ChoiceBox<String> ddTokenDuration;

  @FXML
  private TextField txtTempToken;
  @FXML
  private TextField txtTempIdentityId;
  @FXML
  private TextField txtTempAccessKey;
  @FXML
  private TextField txtTempSecretKey;
  @FXML
  private TextField txtTempSessionToken;

  @FXML
  private TextField txtCognitoIdentityLogin;
  
  @FXML
  private TextField txtHttpRequestData;

  AmazonCognitoIdentityClient client;

  private SpringJavaFxAwsCognitoApplication mainApp;

  /**
   * The constructor. The constructor is called before the initialize() method.
   */
  public MainAppController() {

  }

  @FXML
  private void generateToken(final Event event) {
    getOpenIdToken();
  }

  @FXML
  private void getCredentials(final Event event) {
    getTempCredentials();
  }

  @FXML
  private void reset(final Event event) {
    txtAwsAccessKeyId.clear();
    txtAwsSecretAccessKey.clear();
    txtUsername.clear();
    txtTempToken.clear();
    txtTempAccessKey.clear();
    txtTempIdentityId.clear();
    txtTempSecretKey.clear();
    txtTempSessionToken.clear();
    txtHttpRequestData.clear();
  }

  private void getOpenIdToken() {
    AWSCredentials awsCredentials = new BasicAWSCredentials(txtAwsAccessKeyId.getText(),
        txtAwsSecretAccessKey.getText());
    client = new AmazonCognitoIdentityClient(awsCredentials);
    client.withRegion(Regions.fromName(ddAwsRegion.getValue()));
    GetOpenIdTokenForDeveloperIdentityRequest tokenRequest = new GetOpenIdTokenForDeveloperIdentityRequest();
    tokenRequest.setIdentityPoolId(txtIdentityPoolId.getText());
    HashMap<String, String> map = new HashMap<String, String>();
    map.put(txtDeveloperProviderNameId.getText(), txtUsername.getText());
    tokenRequest.setLogins(map);
    tokenRequest.setTokenDuration(Long.parseLong(ddTokenDuration.getValue()) * 3600);
    GetOpenIdTokenForDeveloperIdentityResult result = client.getOpenIdTokenForDeveloperIdentity(tokenRequest);
    txtTempToken.setText(result.getToken());
    txtTempIdentityId.setText(result.getIdentityId());
  }

  private void getTempCredentials() {
    Map<String, String> logins = new HashMap<>();
    logins.put(txtCognitoIdentityLogin.getText(), txtTempToken.getText());
    GetCredentialsForIdentityRequest getCredentialsRequest = new GetCredentialsForIdentityRequest()
        .withIdentityId(txtTempIdentityId.getText()).withLogins(logins);
    GetCredentialsForIdentityResult getCredentialsResult = client.getCredentialsForIdentity(getCredentialsRequest);
    txtTempAccessKey.setText(getCredentialsResult.getCredentials().getAccessKeyId());
    txtTempSecretKey.setText(getCredentialsResult.getCredentials().getSecretKey());
    txtTempSessionToken.setText(getCredentialsResult.getCredentials().getSessionToken());
  }

  @FXML
  private void sendHttpRequest(final Event event) {
    String url = "https://3rdcwbafha.execute-api.eu-central-1.amazonaws.com/sosdevV2/api/sosdev";
    String payload = "{\n  \"query\": \"query($id:Long!){retrieveAllConsultantTags(consultantId:$id){ id, experienceLevel,pdTag{tagName description},pdConsultant{firstName}}}\",\n  \"variables\": {\n    \"id\": 21\n  }\n}";
    try {
      TreeMap<String, String> awsHeaders = new TreeMap<String, String>();
      awsHeaders.put("host", "3rdcwbafha.execute-api.eu-central-1.amazonaws.com");
      awsHeaders.put("content-type", "application/json");
      awsHeaders.put("content-length", String.valueOf(payload.length()));
      awsHeaders.put("x-amz-security-token", txtTempSessionToken.getText());
      AwsSignedRequest aWSV4Auth = new AwsSignedRequest.Builder(txtTempAccessKey.getText(), txtTempSecretKey.getText())
          .regionName(ddAwsRegion.getValue()).serviceName("execute-api") // es - elastic search. use your service name
          .httpMethodName("POST") // GET, PUT, POST, DELETE, etc...
          .canonicalURI("/sosdevV2/api/sosdev") // end point
          .queryParametes(null) // query parameters if any
          .awsHeaders(awsHeaders) // aws header parameters
          .payload(payload) // payload if any
          .debug() // turn on the debug mode
          .build();

      RestTemplate restTemplate = new RestTemplate();
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      Map<String, String> header = aWSV4Auth.getHeaders();
      for (Map.Entry<String, String> entrySet : header.entrySet()) {
        String key = entrySet.getKey();
        String value = entrySet.getValue();
        headers.add(key, value);
      }
      headers.add("x-amz-security-token", txtTempSessionToken.getText());
      org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<String>(payload,
          headers);

      String answer = restTemplate.postForObject(url, entity, String.class);
      System.out.println(answer.length());
      txtHttpRequestData.setText(answer);

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // TODO Auto-generated method stub
    ddAwsRegion.getItems().add("us-east-1");
    ddAwsRegion.getItems().add("us-east-2");
    ddAwsRegion.getItems().add("us-west-1");
    ddAwsRegion.getItems().add("us-west-2");
    ddAwsRegion.getItems().add("ca-central-1");
    ddAwsRegion.getItems().add("eu-central-1");
    ddAwsRegion.getItems().add("eu-west-1");
    ddAwsRegion.getItems().add("eu-west-2");
    ddAwsRegion.getItems().add("eu-west-3");
    ddAwsRegion.getItems().add("ap-northeast-1");
    ddAwsRegion.getItems().add("ap-northeast-2");
    ddAwsRegion.getItems().add("ap-northeast-3");
    ddAwsRegion.getItems().add("ap-southeast-1");
    ddAwsRegion.getItems().add("ap-southeast-2");
    ddAwsRegion.getItems().add("ap-south-1");
    ddAwsRegion.getItems().add("sa-east-1");
    ddAwsRegion.setValue("eu-central-1");
    ddTokenDuration.getItems().add("1");
    ddTokenDuration.getItems().add("4");
    ddTokenDuration.getItems().add("12");
    ddTokenDuration.getItems().add("24");
    ddTokenDuration.setValue("24");
  }

}
