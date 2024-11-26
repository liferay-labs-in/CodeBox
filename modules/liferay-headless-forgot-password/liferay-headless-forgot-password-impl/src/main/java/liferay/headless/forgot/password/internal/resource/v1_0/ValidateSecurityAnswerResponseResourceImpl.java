package liferay.headless.forgot.password.internal.resource.v1_0;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.NoSuchUserException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.Validator;
import liferay.headless.forgot.password.dto.v1_0.ValidateSecurityAnswerRequest;
import liferay.headless.forgot.password.dto.v1_0.ValidateSecurityAnswerResponse;
import liferay.headless.forgot.password.resource.v1_0.ValidateSecurityAnswerResponseResource;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Ravi Prakash
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/validate-security-answer-response.properties",
	scope = ServiceScope.PROTOTYPE,
	service = ValidateSecurityAnswerResponseResource.class
)
public class ValidateSecurityAnswerResponseResourceImpl
	extends BaseValidateSecurityAnswerResponseResourceImpl {
	@Override
	public ValidateSecurityAnswerResponse postValidateSecurityAnswer(ValidateSecurityAnswerRequest validateSecurityAnswerRequest) throws Exception {

		String email = validateSecurityAnswerRequest.getEmail();
		String answer = validateSecurityAnswerRequest.getAnswer();

		ValidateSecurityAnswerResponse validateSecurityAnswerResponse = new ValidateSecurityAnswerResponse();
		try{
			long companyId= _CompanyLocalService.getCompanies().get(0).getCompanyId();

			User user = _UserLocalService.getUserByEmailAddress(companyId,email);

			if(Validator.isNotNull(user)){

				if(user.getReminderQueryAnswer().equals(answer)){

					UserLocalServiceUtil.sendPasswordByEmailAddress(companyId,email);

					validateSecurityAnswerResponse.setCode(ApiConstant.FP100);
					validateSecurityAnswerResponse.setStatus(ApiConstant.SUCCESS);
					validateSecurityAnswerResponse.setErrorMessage(StringPool.BLANK);
					validateSecurityAnswerResponse.setMessage("Reset password link sent to email :"+email);

				}else{
					validateSecurityAnswerResponse.setCode(ApiConstant.FP103);
					validateSecurityAnswerResponse.setStatus(ApiConstant.FAILED);
					validateSecurityAnswerResponse.setErrorMessage(ApiConstant.getApiResponseMessage(ApiConstant.FP103));
				}
			}else{

			}
		}catch (NoSuchUserException exception) {
			validateSecurityAnswerResponse.setStatus(ApiConstant.FAILED);
			validateSecurityAnswerResponse.setCode(ApiConstant.FP101);
			validateSecurityAnswerResponse.setErrorMessage(ApiConstant.getApiResponseMessage(ApiConstant.FP101));
		}catch(Exception e ){
			validateSecurityAnswerResponse.setStatus(ApiConstant.FAILED);
			validateSecurityAnswerResponse.setCode(ApiConstant.FP102);
			validateSecurityAnswerResponse.setErrorMessage(ApiConstant.getApiResponseMessage(ApiConstant.FP102)+e.getMessage());
		}

		return validateSecurityAnswerResponse;
	}

	@Reference
	UserLocalService _UserLocalService;

	@Reference
	CompanyLocalService _CompanyLocalService;
}