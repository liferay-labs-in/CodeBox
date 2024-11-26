package liferay.headless.forgot.password.internal.graphql.servlet.v1_0;

import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.vulcan.graphql.servlet.ServletData;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import liferay.headless.forgot.password.internal.graphql.mutation.v1_0.Mutation;
import liferay.headless.forgot.password.internal.graphql.query.v1_0.Query;
import liferay.headless.forgot.password.internal.resource.v1_0.GetSecurityQuestionResponseResourceImpl;
import liferay.headless.forgot.password.internal.resource.v1_0.ValidateSecurityAnswerResponseResourceImpl;
import liferay.headless.forgot.password.resource.v1_0.GetSecurityQuestionResponseResource;
import liferay.headless.forgot.password.resource.v1_0.ValidateSecurityAnswerResponseResource;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;

/**
 * @author Ravi Prakash
 * @generated
 */
@Component(service = ServletData.class)
@Generated("")
public class ServletDataImpl implements ServletData {

	@Activate
	public void activate(BundleContext bundleContext) {
		Mutation.setGetSecurityQuestionResponseResourceComponentServiceObjects(
			_getSecurityQuestionResponseResourceComponentServiceObjects);
		Mutation.
			setValidateSecurityAnswerResponseResourceComponentServiceObjects(
				_validateSecurityAnswerResponseResourceComponentServiceObjects);
	}

	public String getApplicationName() {
		return "LiferayHeadlessForgotPassword";
	}

	@Override
	public Mutation getMutation() {
		return new Mutation();
	}

	@Override
	public String getPath() {
		return "/liferay-headless-forgot-password-graphql/v1_0";
	}

	@Override
	public Query getQuery() {
		return new Query();
	}

	public ObjectValuePair<Class<?>, String> getResourceMethodObjectValuePair(
		String methodName, boolean mutation) {

		if (mutation) {
			return _resourceMethodObjectValuePairs.get(
				"mutation#" + methodName);
		}

		return _resourceMethodObjectValuePairs.get("query#" + methodName);
	}

	private static final Map<String, ObjectValuePair<Class<?>, String>>
		_resourceMethodObjectValuePairs =
			new HashMap<String, ObjectValuePair<Class<?>, String>>() {
				{
					put(
						"mutation#createGetSecurityQuestion",
						new ObjectValuePair<>(
							GetSecurityQuestionResponseResourceImpl.class,
							"postGetSecurityQuestion"));
					put(
						"mutation#createValidateSecurityAnswer",
						new ObjectValuePair<>(
							ValidateSecurityAnswerResponseResourceImpl.class,
							"postValidateSecurityAnswer"));
				}
			};

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<GetSecurityQuestionResponseResource>
		_getSecurityQuestionResponseResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<ValidateSecurityAnswerResponseResource>
		_validateSecurityAnswerResponseResourceComponentServiceObjects;

}