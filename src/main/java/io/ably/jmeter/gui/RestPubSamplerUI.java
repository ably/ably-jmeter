package io.ably.jmeter.gui;

import io.ably.jmeter.Constants;
import io.ably.jmeter.samplers.RestPubSampler;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.*;

/**
 * The GUI for the REST publish sampler
 */
public class RestPubSamplerUI extends AbstractSamplerGui implements Constants {
	private CommonUIElements uiElements = new CommonUIElements();
	private static final long serialVersionUID = 1666890646673145131L;

	public RestPubSamplerUI() {
		this.init();
	}

	private void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);
		JPanel mainPanel = new VerticalPanel();
		add(mainPanel, BorderLayout.CENTER);

		mainPanel.add(uiElements.createPubOption());
		mainPanel.add(uiElements.createMessagePayload());
		mainPanel.add(uiElements.createMessageAttributes());
	}

	@Override
	public void configure(TestElement element) {
		super.configure(element);
		RestPubSampler sampler = (RestPubSampler)element;
		uiElements.configurePublisher(sampler);
	}

	@Override
	public TestElement createTestElement() {
		RestPubSampler sampler = new RestPubSampler();
		this.configureTestElement(sampler);
		uiElements.setupSamplerPublishProperties(sampler);
		return sampler;
	}

	@Override
	public String getLabelResource() {
		throw new RuntimeException();
	}

	@Override
	public String getStaticLabel() {
		return "Ably REST Publish";
	}

	@Override
	public void modifyTestElement(TestElement arg0) {
		RestPubSampler sampler = (RestPubSampler)arg0;
		this.configureTestElement(sampler);
		uiElements.setupSamplerPublishProperties(sampler);
	}

	@Override
	public void clearGui() {
		super.clearGui();
		uiElements.clearPublishUI();
	}
}
