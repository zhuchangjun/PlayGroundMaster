
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Vector;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.json.JSONObject;


public class SampleClient {

    public static void main(String[] theArgs) {

        // Create a FHIR client
        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        client.registerInterceptor(new LoggingInterceptor(false));

        // Search for Patient resources
        Bundle response = client
                .search()
                .forResource("Patient")
                .where(Patient.FAMILY.matches().value("SMITH"))
                .returnBundle(Bundle.class)
                .execute();
 
        try
        {
        	//Fetch patient data
        	(new SampleClient()).process(response);
        }
        catch(Exception ex)
        {
        	ex.printStackTrace();
        }
    }

    public void process(Bundle response) throws Exception {
    	HttpClient client;
    	HttpRequest request;
    	HttpResponse<String> res;
    	JSONObject obj;
    	String pid = "";
    	String firstname = "";
    	String lastname = "";
    	String birthDate = "";
    	PatientInfo patientInfo = new PatientInfo();
    	PatientInfo tempPatientInfo = new PatientInfo();
    	Vector<PatientInfo> patientInfoVector = new Vector<PatientInfo>();
    	String tempStr;
    	int lastNo = 0;
    	List<Bundle.BundleEntryComponent> list = response.getEntry();
    	    
        for(int i = 0; i < list.size(); i++)
    	{	
    		client = HttpClient.newHttpClient();
    	    request = HttpRequest.newBuilder()
    	          .uri(URI.create(list.get(i).getFullUrl()))
    	          .build();

    	    res = client.send(request, BodyHandlers.ofString());


    	    String str = res.body();
    	    obj = new JSONObject(str);
    	    try
    	    {
    	    	for (int m = 0; m < (obj.getJSONArray("name")).length(); m++)
    	    	{
                	tempStr = (obj.getJSONArray("name")).get(m).toString();
                	firstname = tempStr.substring(tempStr.indexOf("given") + 9, tempStr.indexOf(",") - 2);
                	lastname = tempStr.substring(tempStr.indexOf("family") + 9, tempStr.indexOf("}") - 1);
    	    	}
    	    	pid = obj.getString("id");
    	    	birthDate = obj.getString("birthDate");
    	    	if (birthDate.length() > 10) birthDate = birthDate.substring(0,10);
    	    }
    	    catch(Exception ex)
    	    {
//    	    	ex.printStackTrace();
    	    	birthDate = "2099-01-01";
    	    }
    	    patientInfo = new PatientInfo();
    	    patientInfo.setPid(pid);
    	    patientInfo.setFirstname(firstname);
    	    patientInfo.setLastname(lastname);
    	    patientInfo.setBirthDate(birthDate);
    	    patientInfoVector.add(patientInfo);
    	}
        for(int i = 0; i < patientInfoVector.size(); i++)
        {
        	tempPatientInfo = patientInfoVector.get(i);
        	for(int j = i + 1; j < patientInfoVector.size(); j++)
        	{
        		if(tempPatientInfo.getFirstname().toUpperCase().compareTo(patientInfoVector.get(j).getFirstname().toUpperCase()) > 0)
        		{
        			tempPatientInfo = patientInfoVector.get(j);
        			lastNo = j;
        		}
        	}	
        	patientInfoVector.set(lastNo, patientInfoVector.get(i));
        	patientInfoVector.set(i, tempPatientInfo);
        }
        for(int k = 0; k < patientInfoVector.size(); k++)
        {
        	patientInfo = patientInfoVector.get(k);
        	System.out.println("PatientID = " + patientInfo.getPid() + "   " + "firstname = " + patientInfo.getFirstname() + "   " + "lastname = " + patientInfo.getLastname() + "   " + "birthDate = " + patientInfo.getBirthDate());
        }	
    }
}


