package io.ably.jmeter.gui;

import io.ably.jmeter.Constants;
import io.ably.jmeter.samplers.RestSampler;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.*;

/**
 * The GUI for the REST configuration sampler
 */
public class RestUI extends AbstractSamplerGui implements Constants {
	private CommonUIElements uiElements = new CommonUIElements();
	private static final long serialVersionUID = 1666890646673145131L;

	public RestUI() {
		this.init();
	}

	private void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);
		JPanel mainPanel = new VerticalPanel();
		add(mainPanel, BorderLayout.CENTER);

		mainPanel.add(uiElements.createConnPanel());
		mainPanel.add(uiElements.createAuthentication());
		mainPanel.add(uiElements.createClientOptions());
		mainPanel.add(uiElements.createLibraryOptions());
	}

	@Override
	public void configure(TestElement element) {
		super.configure(element);
		RestSampler sampler = (RestSampler)element;
		uiElements.configureClient(sampler);
	}

	@Override
	public TestElement createTestElement() {
		RestSampler sampler = new RestSampler();
		this.configureTestElement(sampler);
		uiElements.setupSamplerClientProperties(sampler);
		return sampler;
	}

	@Override
	public String getLabelResource() {
		throw new RuntimeException();
	}

	@Override
	public String getStaticLabel() {
		return "Ably REST Configuration";
	}

	@Override
	public void modifyTestElement(TestElement arg0) {
		RestSampler sampler = (RestSampler)arg0;
		this.configureTestElement(sampler);
		uiElements.setupSamplerClientProperties(sampler);
	}

	@Override
	public void clearGui() {
		super.clearGui();
		uiElements.clearClientUI();
	}
}
