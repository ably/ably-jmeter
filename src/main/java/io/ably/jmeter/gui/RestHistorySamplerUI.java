package io.ably.jmeter.gui;

import io.ably.jmeter.Constants;
import io.ably.jmeter.samplers.RestHistorySampler;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * The GUI for the REST stats sampler
 */
public class RestHistorySamplerUI extends AbstractSamplerGui implements Constants, ChangeListener {
	private static final long serialVersionUID = 1666890646673145131L;

	private final JLabeledTextField channelName = new JLabeledTextField("Channel name:");
	private final JLabeledTextField start = new JLabeledTextField("Start:");
	private final JLabeledTextField end = new JLabeledTextField("End:");
	private final JLabeledTextField limit = new JLabeledTextField("Limit:");
	private final JLabeledChoice direction = new JLabeledChoice("Direction:", Constants.DIRECTIONS.split(","));

	public RestHistorySamplerUI() {
		this.init();
	}

	private void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);
		JPanel mainPanel = new VerticalPanel();
		add(mainPanel, BorderLayout.CENTER);

		JPanel channelPanel = new VerticalPanel();
		channelPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Channel"));
		channelPanel.add(channelName);
		mainPanel.add(channelPanel);

		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Request params"));
		JPanel startPanel = new HorizontalPanel();
		startPanel.add(start);
		optsPanelCon.add(startPanel);

		JPanel endPanel = new HorizontalPanel();
		endPanel.add(end);
		optsPanelCon.add(endPanel);

		JPanel limitPanel = new HorizontalPanel();
		limitPanel.add(limit);
		optsPanelCon.add(limitPanel);

		JPanel directionPanel = new HorizontalPanel();
		direction.setToolTipText("Select the direction of the stats request");
		direction.addChangeListener(this);
		directionPanel.add(direction);
		optsPanelCon.add(directionPanel);

		mainPanel.add(optsPanelCon);
	}

	@Override
	public void configure(TestElement element) {
		super.configure(element);
		RestHistorySampler sampler = (RestHistorySampler)element;
		channelName.setText(sampler.getChannelPrefix());
		start.setText(sampler.getHistoryStart());
		end.setText(sampler.getHistoryEnd());
		limit.setText(sampler.getHistoryLimit());
		direction.setSelectedIndex(sampler.getHistoryDirectionIndex());
	}

	@Override
	public TestElement createTestElement() {
		RestHistorySampler sampler = new RestHistorySampler();
		this.configureTestElement(sampler);
		sampler.setChannelPrefix(channelName.getText());
		sampler.setHistoryStart(start.getText());
		sampler.setHistoryEnd(end.getText());
		sampler.setHistoryLimit(limit.getText());
		sampler.setHistoryDirectionIndex(direction.getSelectedIndex());
		return sampler;
	}

	@Override
	public String getLabelResource() {
		throw new RuntimeException();
	}

	@Override
	public String getStaticLabel() {
		return "Ably REST History";
	}

	@Override
	public void modifyTestElement(TestElement arg0) {
		RestHistorySampler sampler = (RestHistorySampler)arg0;
		this.configureTestElement(sampler);
		sampler.setChannelPrefix(channelName.getText());
		sampler.setHistoryStart(start.getText());
		sampler.setHistoryEnd(end.getText());
		sampler.setHistoryLimit(limit.getText());
		sampler.setHistoryDirectionIndex(direction.getSelectedIndex());
	}

	@Override
	public void clearGui() {
		super.clearGui();
		channelName.setText(DEFAULT_CHANNEL_NAME_PREFIX);
		start.setText(DEFAULT_HISTORY_START);
		end.setText(DEFAULT_HISTORY_END);
		limit.setText(DEFAULT_HISTORY_LIMIT);
		direction.setSelectedIndex(DEFAULT_HISTORY_DIRECTION);
	}

	@Override
	public void stateChanged(ChangeEvent e) {}
}
