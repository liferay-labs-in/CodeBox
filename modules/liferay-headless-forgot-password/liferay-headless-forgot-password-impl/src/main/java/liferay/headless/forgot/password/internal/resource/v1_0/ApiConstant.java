package liferay.headless.forgot.password.internal.resource.v1_0;

import java.util.HashMap;
import java.util.Map;

public class ApiConstant {

    public static final String SUCCESS = "Success";
    public static final String FAILED = "Failed";
    public static final String FP100 = "FP100";
    public static final String FP101 = "FP101";
    public static final String FP102 = "FP102";
    public static final String FP103 = "FP103";


    /**
     *
     * This method is used to get response message for all api codes
     *
     * @param key : status code key
     * @return : response message
     */
    public static String getApiResponseMessage(String key) {
        Map<String, String> messageResponse = new HashMap<>();
        messageResponse.put(FP100, "Request Processed Successfully");
        messageResponse.put(FP101, "This Email doesn't exist with the system");
        messageResponse.put(FP102, "Error in Processing request : ");
        messageResponse.put(FP103, "Security answer provided is Incorrect ");
        return messageResponse.get(key);
    }
}
