package liferay.headless.forgot.password.internal.graphql.mutation.v1_0;

import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.function.UnsafeFunction;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLField;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLName;

import java.util.function.BiFunction;

import javax.annotation.Generated;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.core.UriInfo;

import liferay.headless.forgot.password.dto.v1_0.GetSecurityQuestionRequest;
import liferay.headless.forgot.password.dto.v1_0.GetSecurityQuestionResponse;
import liferay.headless.forgot.password.dto.v1_0.ValidateSecurityAnswerRequest;
import liferay.headless.forgot.password.dto.v1_0.ValidateSecurityAnswerResponse;
import liferay.headless.forgot.password.resource.v1_0.GetSecurityQuestionResponseResource;
import liferay.headless.forgot.password.resource.v1_0.ValidateSecurityAnswerResponseResource;

import org.osgi.service.component.ComponentServiceObjects;

/**
 * @author Ravi Prakash
 * @generated
 */
@Generated("")
public class Mutation {

	public static void
		setGetSecurityQuestionResponseResourceComponentServiceObjects(
			ComponentServiceObjects<GetSecurityQuestionResponseResource>
				getSecurityQuestionResponseResourceComponentServiceObjects) {

		_getSecurityQuestionResponseResourceComponentServiceObjects =
			getSecurityQuestionResponseResourceComponentServiceObjects;
	}

	public static void
		setValidateSecurityAnswerResponseResourceComponentServiceObjects(
			ComponentServiceObjects<ValidateSecurityAnswerResponseResource>
				validateSecurityAnswerResponseResourceComponentServiceObjects) {

		_validateSecurityAnswerResponseResourceComponentServiceObjects =
			validateSecurityAnswerResponseResourceComponentServiceObjects;
	}

	@GraphQLField(
		description = "Get the security question for the provided email"
	)
	public GetSecurityQuestionResponse createGetSecurityQuestion(
			@GraphQLName("getSecurityQuestionRequest")
				GetSecurityQuestionRequest getSecurityQuestionRequest)
		throws Exception {

		return _applyComponentServiceObjects(
			_getSecurityQuestionResponseResourceComponentServiceObjects,
			this::_populateResourceContext,
			getSecurityQuestionResponseResource ->
				getSecurityQuestionResponseResource.postGetSecurityQuestion(
					getSecurityQuestionRequest));
	}

	@GraphQLField(
		description = "Validate the provided security answer for the given email"
	)
	public ValidateSecurityAnswerResponse createValidateSecurityAnswer(
			@GraphQLName("validateSecurityAnswerRequest")
				ValidateSecurityAnswerRequest validateSecurityAnswerRequest)
		throws Exception {

		return _applyComponentServiceObjects(
			_validateSecurityAnswerResponseResourceComponentServiceObjects,
			this::_populateResourceContext,
			validateSecurityAnswerResponseResource ->
				validateSecurityAnswerResponseResource.
					postValidateSecurityAnswer(validateSecurityAnswerRequest));
	}

	private <T, R, E1 extends Throwable, E2 extends Throwable> R
			_applyComponentServiceObjects(
				ComponentServiceObjects<T> componentServiceObjects,
				UnsafeConsumer<T, E1> unsafeConsumer,
				UnsafeFunction<T, R, E2> unsafeFunction)
		throws E1, E2 {

		T resource = componentServiceObjects.getService();

		try {
			unsafeConsumer.accept(resource);

			return unsafeFunction.apply(resource);
		}
		finally {
			componentServiceObjects.ungetService(resource);
		}
	}

	private <T, E1 extends Throwable, E2 extends Throwable> void
			_applyVoidComponentServiceObjects(
				ComponentServiceObjects<T> componentServiceObjects,
				UnsafeConsumer<T, E1> unsafeConsumer,
				UnsafeConsumer<T, E2> unsafeFunction)
		throws E1, E2 {

		T resource = componentServiceObjects.getService();

		try {
			unsafeConsumer.accept(resource);

			unsafeFunction.accept(resource);
		}
		finally {
			componentServiceObjects.ungetService(resource);
		}
	}

	private void _populateResourceContext(
			GetSecurityQuestionResponseResource
				getSecurityQuestionResponseResource)
		throws Exception {

		getSecurityQuestionResponseResource.setContextAcceptLanguage(
			_acceptLanguage);
		getSecurityQuestionResponseResource.setContextCompany(_company);
		getSecurityQuestionResponseResource.setContextHttpServletRequest(
			_httpServletRequest);
		getSecurityQuestionResponseResource.setContextHttpServletResponse(
			_httpServletResponse);
		getSecurityQuestionResponseResource.setContextUriInfo(_uriInfo);
		getSecurityQuestionResponseResource.setContextUser(_user);
		getSecurityQuestionResponseResource.setGroupLocalService(
			_groupLocalService);
		getSecurityQuestionResponseResource.setRoleLocalService(
			_roleLocalService);
	}

	private void _populateResourceContext(
			ValidateSecurityAnswerResponseResource
				validateSecurityAnswerResponseResource)
		throws Exception {

		validateSecurityAnswerResponseResource.setContextAcceptLanguage(
			_acceptLanguage);
		validateSecurityAnswerResponseResource.setContextCompany(_company);
		validateSecurityAnswerResponseResource.setContextHttpServletRequest(
			_httpServletRequest);
		validateSecurityAnswerResponseResource.setContextHttpServletResponse(
			_httpServletResponse);
		validateSecurityAnswerResponseResource.setContextUriInfo(_uriInfo);
		validateSecurityAnswerResponseResource.setContextUser(_user);
		validateSecurityAnswerResponseResource.setGroupLocalService(
			_groupLocalService);
		validateSecurityAnswerResponseResource.setRoleLocalService(
			_roleLocalService);
	}

	private static ComponentServiceObjects<GetSecurityQuestionResponseResource>
		_getSecurityQuestionResponseResourceComponentServiceObjects;
	private static ComponentServiceObjects
		<ValidateSecurityAnswerResponseResource>
			_validateSecurityAnswerResponseResourceComponentServiceObjects;

	private AcceptLanguage _acceptLanguage;
	private com.liferay.portal.kernel.model.Company _company;
	private GroupLocalService _groupLocalService;
	private HttpServletRequest _httpServletRequest;
	private HttpServletResponse _httpServletResponse;
	private RoleLocalService _roleLocalService;
	private BiFunction<Object, String, Sort[]> _sortsBiFunction;
	private UriInfo _uriInfo;
	private com.liferay.portal.kernel.model.User _user;

}