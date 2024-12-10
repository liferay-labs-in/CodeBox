package liferay.headless.forgot.password.internal.resource.v1_0;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.NoSuchUserException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.Validator;
import liferay.headless.forgot.password.dto.v1_0.GetSecurityQuestionRequest;
import liferay.headless.forgot.password.dto.v1_0.GetSecurityQuestionResponse;
import liferay.headless.forgot.password.resource.v1_0.GetSecurityQuestionResponseResource;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Ravi Prakash
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/get-security-question-response.properties",
	scope = ServiceScope.PROTOTYPE,
	service = GetSecurityQuestionResponseResource.class
)
public class GetSecurityQuestionResponseResourceImpl
	extends BaseGetSecurityQuestionResponseResourceImpl {

	@Override
	public GetSecurityQuestionResponse postGetSecurityQuestion(GetSecurityQuestionRequest getSecurityQuestionRequest) throws Exception {
		String email = getSecurityQuestionRequest.getEmail();
		GetSecurityQuestionResponse getSecurityQuestionResponse=new GetSecurityQuestionResponse();
		try{

			long companyId=_CompanyLocalService.getCompanies().get(0).getCompanyId();

			User user = _UserLocalService.getUserByEmailAddress(companyId,email);
			//UserLocalServiceUtil.sendPasswordByEmailAddress();

			if(Validator.isNotNull(user)){
				getSecurityQuestionResponse.setQuestion(user.getReminderQueryQuestion());
				getSecurityQuestionResponse.setStatus(ApiConstant.SUCCESS);
				getSecurityQuestionResponse.setCode(ApiConstant.FP100);
				getSecurityQuestionResponse.setErrorMessage(StringPool.BLANK);
			}else{
				getSecurityQuestionResponse.setStatus(ApiConstant.FAILED);
				getSecurityQuestionResponse.setCode(ApiConstant.FP101);
				getSecurityQuestionResponse.setErrorMessage(ApiConstant.getApiResponseMessage(ApiConstant.FP101));
			}

		}catch (NoSuchUserException exception){
			getSecurityQuestionResponse.setStatus(ApiConstant.FAILED);
			getSecurityQuestionResponse.setCode(ApiConstant.FP101);
			getSecurityQuestionResponse.setErrorMessage(ApiConstant.getApiResponseMessage(ApiConstant.FP101));
		}
		catch (Exception e){
			e.printStackTrace();
			getSecurityQuestionResponse.setStatus(ApiConstant.FAILED);
			getSecurityQuestionResponse.setCode(ApiConstant.FP102);
			getSecurityQuestionResponse.setErrorMessage(ApiConstant.getApiResponseMessage(ApiConstant.FP102)+e.getMessage());
		}


		return getSecurityQuestionResponse;
	}
	@Reference
	UserLocalService _UserLocalService;

	@Reference
	CompanyLocalService _CompanyLocalService;
}