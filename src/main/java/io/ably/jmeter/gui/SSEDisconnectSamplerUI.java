package io.ably.jmeter.gui;

import io.ably.jmeter.Constants;
import io.ably.jmeter.samplers.SSEDisconnectSampler;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.*;

/**
 * The GUI for the Disconnect sampler
 */
public class SSEDisconnectSamplerUI extends AbstractSamplerGui implements Constants {
	private static final long serialVersionUID = 1666890646673145131L;

	public SSEDisconnectSamplerUI() {
		this.init();
	}

	private void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());
		add(makeTitlePanel(), BorderLayout.NORTH);
		JPanel mainPanel = new VerticalPanel();
		add(mainPanel, BorderLayout.CENTER);
	}

	@Override
	public void configure(TestElement element) {
		super.configure(element);
	}

	@Override
	public TestElement createTestElement() {
		SSEDisconnectSampler sampler = new SSEDisconnectSampler();
		this.configureTestElement(sampler);
		return sampler;
	}

	@Override
	public String getLabelResource() {
		throw new RuntimeException();
	}

	@Override
	public String getStaticLabel() {
		return "Ably SSE Disconnect";
	}

	@Override
	public void modifyTestElement(TestElement arg0) {
		SSEDisconnectSampler sampler = (SSEDisconnectSampler)arg0;
		this.configureTestElement(sampler);
	}

	@Override
	public void clearGui() {
		super.clearGui();
	}
}
