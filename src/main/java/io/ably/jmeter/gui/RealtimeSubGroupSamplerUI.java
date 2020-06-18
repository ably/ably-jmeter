package io.ably.jmeter.gui;

import io.ably.jmeter.Constants;
import io.ably.jmeter.samplers.RealtimeSubGroupSampler;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * The GUI for the realtime subscribe sampler
 */
public class RealtimeSubGroupSamplerUI extends AbstractSamplerGui implements Constants, ChangeListener{
	private CommonUIElements uiElements = new CommonUIElements();
	private static final long serialVersionUID = 1715399546099472610L;
	private static final Logger logger = LoggerFactory.getLogger(RealtimeSubGroupSamplerUI.class.getCanonicalName());

	public RealtimeSubGroupSamplerUI() {
		this.init();
	}

	private void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);
		JPanel mainPanel = new VerticalPanel();
		add(mainPanel, BorderLayout.CENTER);

		mainPanel.add(uiElements.createSubOption());
	}

	@Override
	public String getStaticLabel() {
		return "Ably Realtime Subscribe Group";
	}

	@Override
	public TestElement createTestElement() {
		RealtimeSubGroupSampler sampler = new RealtimeSubGroupSampler();
		this.setupSamplerProperties(sampler);
		return sampler;
	}

	@Override
	public void configure(TestElement element) {
		super.configure(element);
		RealtimeSubGroupSampler sampler = (RealtimeSubGroupSampler) element;
		uiElements.configureSubscriber(sampler);
	}

	@Override
	public String getLabelResource() {
		return "";
	}

	@Override
	public void modifyTestElement(TestElement arg0) {
		RealtimeSubGroupSampler sampler = (RealtimeSubGroupSampler) arg0;
		this.setupSamplerProperties(sampler);
	}

	private void setupSamplerProperties(RealtimeSubGroupSampler sampler) {
		this.configureTestElement(sampler);
		uiElements.setupSamplerSubscribeProperties(sampler);
	}

	@Override
	public void clearGui() {
		super.clearGui();
		uiElements.clearSubscribeUI();
	}

	@Override
	public void stateChanged(ChangeEvent e) {}
}
