package org.skyve.wildcat.metadata.repository.customer;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.skyve.domain.types.DateOnly;
import org.skyve.domain.types.DateTime;
import org.skyve.domain.types.TimeOnly;
import org.skyve.domain.types.Timestamp;
import org.skyve.domain.types.converters.Converter;
import org.skyve.metadata.ConverterName;
import org.skyve.metadata.MetaDataException;
import org.skyve.metadata.customer.Customer;
import org.skyve.metadata.model.Attribute.AttributeType;
import org.skyve.wildcat.metadata.customer.CustomerImpl;
import org.skyve.wildcat.metadata.repository.NamedMetaData;
import org.skyve.wildcat.metadata.repository.PersistentMetaData;
import org.skyve.wildcat.util.UtilImpl;
import org.skyve.wildcat.util.XMLUtil;

// TODO Populate defaultActions property in customer returned by convert
@XmlRootElement(namespace = XMLUtil.CUSTOMER_NAMESPACE, name = "customer")
@XmlType(namespace = XMLUtil.CUSTOMER_NAMESPACE, 
			name = "customer",
			propOrder = {"uiResources",
							"htmlResources", 
							"loginResources", 
							"defaultDateConverter", 
							"defaultTimeConverter",
							"defaultDateTimeConverter", 
							"defaultTimestampConverter", 
							"modules", 
							"homeModule", 
							"services"})
public class CustomerMetaData extends NamedMetaData implements PersistentMetaData<Customer> {
	private UIResources uiResources;
	private HTMLResources htmlResources;
	private LoginResources loginResources;
	private ConverterName defaultDateConverter;
	private ConverterName defaultTimeConverter;
	private ConverterName defaultDateTimeConverter;
	private ConverterName defaultTimestampConverter;
	private List<CustomerModuleMetaData> modules = new ArrayList<>();
	private List<Service> services = new ArrayList<>();
	private String homeModule;

	public UIResources getUiResources() {
		return uiResources;
	}

	@XmlElement(namespace = XMLUtil.CUSTOMER_NAMESPACE)
	public void setUiResources(UIResources uiResources) {
		this.uiResources = uiResources;
	}

	public HTMLResources getHtmlResources() {
		return htmlResources;
	}

	@XmlElement(namespace = XMLUtil.CUSTOMER_NAMESPACE)
	public void setHtmlResources(HTMLResources htmlResources) {
		this.htmlResources = htmlResources;
	}

	public LoginResources getLoginResources() {
		return loginResources;
	}

	@XmlElement(namespace = XMLUtil.CUSTOMER_NAMESPACE)
	public void setLoginResources(LoginResources loginResources) {
		this.loginResources = loginResources;
	}

	public ConverterName getDefaultDateConverter() {
		return defaultDateConverter;
	}

	@XmlElement(namespace = XMLUtil.CUSTOMER_NAMESPACE, required = true)
	public void setDefaultDateConverter(ConverterName defaultDateConverter) {
		this.defaultDateConverter = defaultDateConverter;
	}

	public ConverterName getDefaultDateTimeConverter() {
		return defaultDateTimeConverter;
	}

	@XmlElement(namespace = XMLUtil.CUSTOMER_NAMESPACE, required = true)
	public void setDefaultDateTimeConverter(ConverterName defaultDateTimeConverter) {
		this.defaultDateTimeConverter = defaultDateTimeConverter;
	}

	public ConverterName getDefaultTimeConverter() {
		return defaultTimeConverter;
	}

	@XmlElement(namespace = XMLUtil.CUSTOMER_NAMESPACE, required = true)
	public void setDefaultTimeConverter(ConverterName defaultTimeConverter) {
		this.defaultTimeConverter = defaultTimeConverter;
	}

	public ConverterName getDefaultTimestampConverter() {
		return defaultTimestampConverter;
	}

	@XmlElement(namespace = XMLUtil.CUSTOMER_NAMESPACE, required = true)
	public void setDefaultTimestampConverter(ConverterName defaultTimestampConverter) {
		this.defaultTimestampConverter = defaultTimestampConverter;
	}

	@XmlElementWrapper(namespace = XMLUtil.CUSTOMER_NAMESPACE, name = "modules")
	@XmlElement(namespace = XMLUtil.CUSTOMER_NAMESPACE, name = "module", required = true)
	public List<CustomerModuleMetaData> getModules() {
		return modules;
	}

	@XmlElementWrapper(namespace = XMLUtil.CUSTOMER_NAMESPACE, name = "services")
	@XmlElement(namespace = XMLUtil.CUSTOMER_NAMESPACE, name = "service", required = true)
	public List<Service> getServices() {
		return services;
	}

	public String getHomeModule() {
		return homeModule;
	}

	@XmlElement(namespace = XMLUtil.CUSTOMER_NAMESPACE, required = true)
	public void setHomeModule(String homeModule) {
		this.homeModule = UtilImpl.processStringValue(homeModule);
	}

	@Override
	public CustomerImpl convert(String metaDataName) throws MetaDataException {
		CustomerImpl result = new CustomerImpl();
		String value = getName();
		if (value == null) {
			throw new MetaDataException(metaDataName + " : The customer [name] is required");
		}
		result.setName(value);

		result.setUiResources((uiResources == null) ? new UIResources() : uiResources);
		result.setHtmlResources((htmlResources == null) ? new HTMLResources() : htmlResources);
		result.setLoginResources((loginResources == null) ? new LoginResources() : loginResources);

		ConverterName converterName = getDefaultDateConverter();
		if (converterName == null) {
			throw new MetaDataException(metaDataName + " : The customer [defaultDateConverter] is required");
		}
		@SuppressWarnings("unchecked")
		Converter<DateOnly> dateConverter = (Converter<DateOnly>) converterName.getConverter();
		if (! AttributeType.date.equals(dateConverter.getAttributeType())) {
			throw new MetaDataException(metaDataName + " : The customer [defaultDateConverter] is not for the date type");
		}
		result.setDefaultDateConverter(dateConverter);

		converterName = getDefaultTimeConverter();
		if (converterName == null) {
			throw new MetaDataException(metaDataName + " : The customer [defaultTimeConverter] is required");
		}
		@SuppressWarnings("unchecked")
		Converter<TimeOnly> timeConverter = (Converter<TimeOnly>) converterName.getConverter();
		if (! AttributeType.time.equals(timeConverter.getAttributeType())) {
			throw new MetaDataException(metaDataName + " : The customer [defaultTimeConverter] is not for the time type");
		}
		result.setDefaultTimeConverter(timeConverter);

		converterName = getDefaultDateTimeConverter();
		if (converterName == null) {
			throw new MetaDataException(metaDataName + " : The customer [defaultDateTimeConverter] is required");
		}
		@SuppressWarnings("unchecked")
		Converter<DateTime> dateTimeConverter = (Converter<DateTime>) converterName.getConverter();
		if (! AttributeType.dateTime.equals(dateTimeConverter.getAttributeType())) {
			throw new MetaDataException(metaDataName + " : The customer [defaultDateTimeConverter] is not for the dateTime type");
		}
		result.setDefaultDateTimeConverter(dateTimeConverter);

		converterName = getDefaultTimestampConverter();
		if (converterName == null) {
			throw new MetaDataException(metaDataName + " : The customer [defaultTimestampConverter] is required");
		}
		@SuppressWarnings("unchecked")
		Converter<Timestamp> timestampConverter = (Converter<Timestamp>) converterName.getConverter();
		if (! AttributeType.timestamp.equals(timestampConverter.getAttributeType())) {
			throw new MetaDataException(metaDataName + " : The customer [defaultTimestampConverter] is not for the timestamp type");
		}
		result.setDefaultTimestampConverter(timestampConverter);

		List<String> moduleNames = result.getModuleNames();
		for (CustomerModuleMetaData module : getModules()) {
			if (module == null) {
				throw new MetaDataException(metaDataName + " : One of the module references is not defined.");
			}
			moduleNames.add(module.getName());
		}

		value = getHomeModule();
		if (value == null) {
			throw new MetaDataException(metaDataName + " : The customer [homeModule] is required");
		}
		result.setHomeModuleName(value);

		// Populate Services
		List<Service> repositoryServices = getServices();
		if (repositoryServices != null) {
			for (Service service : repositoryServices) {
				value = service.getName();
				if (value == null) {
					throw new MetaDataException(metaDataName + " : The [name] for a service is required");
				}
				if (! result.putService(service)) {
					throw new MetaDataException(metaDataName + " : Duplicate service named " + value);
				}
				result.putService(service);
			}
		}

		return result;
	}
}