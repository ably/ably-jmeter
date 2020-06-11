package io.ably.jmeter.gui;

import io.ably.jmeter.Constants;
import io.ably.jmeter.samplers.RealtimePubSampler;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.logging.Logger;

/**
 * The GUI for the realtime publish sampler
 */
public class RealtimePubSamplerUI extends AbstractSamplerGui implements Constants, ChangeListener {
	private CommonUIElements uiElements = new CommonUIElements();
	private static final long serialVersionUID = 2479085966683186422L;
	private static final Logger logger = Logger.getLogger(RealtimePubSamplerUI.class.getCanonicalName());

	public RealtimePubSamplerUI() {
		init();
	}

	private void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);
		JPanel mainPanel = new VerticalPanel();
		add(mainPanel, BorderLayout.CENTER);

		mainPanel.add(uiElements.createPubOption());
		mainPanel.add(uiElements.createPayload());
	}

	@Override
	public String getStaticLabel() {
		return "Ably Realtime Publish";
	}

	@Override
	public void stateChanged(ChangeEvent e) {}

	@Override
	public String getLabelResource() {
		return "";
	}

	@Override
	public TestElement createTestElement() {
		RealtimePubSampler sampler = new RealtimePubSampler();
		this.setupSamplerProperties(sampler);
		return sampler;
	}

	@Override
	public void configure(TestElement element) {
		super.configure(element);
		RealtimePubSampler sampler = (RealtimePubSampler) element;
		uiElements.configurePublisher(sampler);
	}

	@Override
	public void modifyTestElement(TestElement arg0) {
		RealtimePubSampler sampler = (RealtimePubSampler) arg0;
		this.setupSamplerProperties(sampler);
	}

	private void setupSamplerProperties(RealtimePubSampler sampler) {
		this.configureTestElement(sampler);
		uiElements.setupSamplerPublishProperties(sampler);
	}

	@Override
	public void clearGui() {
		super.clearGui();
		uiElements.clearPublishUI();
	}
}
