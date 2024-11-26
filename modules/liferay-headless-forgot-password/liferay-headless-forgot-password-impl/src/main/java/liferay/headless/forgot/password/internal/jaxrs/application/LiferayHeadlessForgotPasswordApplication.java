package liferay.headless.forgot.password.internal.jaxrs.application;

import javax.annotation.Generated;

import javax.ws.rs.core.Application;

import org.osgi.service.component.annotations.Component;

/**
 * @author Ravi Prakash
 * @generated
 */
@Component(
	property = {
		"liferay.jackson=false",
		"osgi.jaxrs.application.base=/liferay-headless-forgot-password",
		"osgi.jaxrs.extension.select=(osgi.jaxrs.name=Liferay.Vulcan)",
		"osgi.jaxrs.name=LiferayHeadlessForgotPassword"
	},
	service = Application.class
)
@Generated("")
public class LiferayHeadlessForgotPasswordApplication extends Application {
}