package io.ably.jmeter.gui;

import io.ably.jmeter.Constants;
import io.ably.jmeter.samplers.RealtimeSubSampler;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * The GUI for the realtime subscribe sampler
 */
public class RealtimeSubSamplerUI extends AbstractSamplerGui implements Constants, ChangeListener{
	private CommonUIElements uiElements = new CommonUIElements();
	private static final long serialVersionUID = 1715399546099472610L;
	private static final Logger logger = LoggerFactory.getLogger(RealtimeSubSamplerUI.class.getCanonicalName());

	public RealtimeSubSamplerUI() {
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
		return "Ably Realtime Subscribe";
	}

	@Override
	public TestElement createTestElement() {
		RealtimeSubSampler sampler = new RealtimeSubSampler();
		this.setupSamplerProperties(sampler);
		return sampler;
	}

	@Override
	public void configure(TestElement element) {
		super.configure(element);
		RealtimeSubSampler sampler = (RealtimeSubSampler) element;
		uiElements.configureSubscriber(sampler);
	}

	@Override
	public String getLabelResource() {
		return "";
	}

	@Override
	public void modifyTestElement(TestElement arg0) {
		RealtimeSubSampler sampler = (RealtimeSubSampler) arg0;
		this.setupSamplerProperties(sampler);
	}

	private void setupSamplerProperties(RealtimeSubSampler sampler) {
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
