package io.ably.jmeter.gui;

import io.ably.jmeter.Constants;
import io.ably.jmeter.samplers.SSEConnectSampler;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.*;

/**
 * The GUI for the connect sampler
 */
public class SSEConnectSamplerUI extends AbstractSamplerGui implements Constants {
	private CommonUIElements uiElements = new CommonUIElements();
	private static final long serialVersionUID = 1666890646673145131L;

	public SSEConnectSamplerUI() {
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
		mainPanel.add(uiElements.createSubOption());
	}

	@Override
	public void configure(TestElement element) {
		super.configure(element);
		SSEConnectSampler sampler = (SSEConnectSampler) element;
		uiElements.configureClient(sampler);
		uiElements.configureSubscriber(sampler);
	}

	@Override
	public TestElement createTestElement() {
		SSEConnectSampler sampler = new SSEConnectSampler();
		this.configureTestElement(sampler);
		uiElements.setupSamplerClientProperties(sampler);
		uiElements.setupSamplerSubscribeProperties(sampler);
		return sampler;
	}

	@Override
	public String getLabelResource() {
		throw new RuntimeException();
	}

	@Override
	public String getStaticLabel() {
		return "Ably SSE Connect";
	}

	@Override
	public void modifyTestElement(TestElement arg0) {
		SSEConnectSampler sampler = (SSEConnectSampler) arg0;
		this.configureTestElement(sampler);
		uiElements.setupSamplerClientProperties(sampler);
		uiElements.setupSamplerSubscribeProperties(sampler);
	}

	@Override
	public void clearGui() {
		super.clearGui();
		uiElements.clearClientUI();
		uiElements.clearSubscribeUI();
	}
}
