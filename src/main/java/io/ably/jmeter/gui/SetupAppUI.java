package io.ably.jmeter.gui;

import io.ably.jmeter.Constants;
import io.ably.jmeter.samplers.SetupAppSampler;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.*;

/**
 * The GUI for the SetupApp configuration sampler
 */
public class SetupAppUI extends AbstractSamplerGui implements Constants {
	private CommonUIElements uiElements = new CommonUIElements();
	private static final long serialVersionUID = 1666890646673145131L;

	public SetupAppUI() {
		this.init();
	}

	private void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);
		JPanel mainPanel = new VerticalPanel();
		add(mainPanel, BorderLayout.CENTER);

		mainPanel.add(uiElements.createConnPanel());
	}

	@Override
	public void configure(TestElement element) {
		super.configure(element);
		SetupAppSampler sampler = (SetupAppSampler)element;
		uiElements.configureEnvironment(sampler);
	}

	@Override
	public TestElement createTestElement() {
		SetupAppSampler sampler = new SetupAppSampler();
		this.configureTestElement(sampler);
		uiElements.setupSamplerEnvironmentProperties(sampler);
		return sampler;
	}

	@Override
	public String getLabelResource() {
		throw new RuntimeException();
	}

	@Override
	public String getStaticLabel() {
		return "Ably Create Test App";
	}

	@Override
	public void modifyTestElement(TestElement arg0) {
		SetupAppSampler sampler = (SetupAppSampler)arg0;
		this.configureTestElement(sampler);
		uiElements.setupSamplerEnvironmentProperties(sampler);
	}

	@Override
	public void clearGui() {
		super.clearGui();
		uiElements.clearEnvironmentUI();
	}
}
