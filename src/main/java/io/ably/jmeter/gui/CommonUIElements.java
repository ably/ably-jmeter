package io.ably.jmeter.gui;

import io.ably.jmeter.Constants;
import io.ably.jmeter.samplers.AbstractAblySampler;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

/**
 * Common code for UI elements used across multiple sampler GUIs
 */
public class CommonUIElements implements ChangeListener, ActionListener, Constants {
	private static final Logger logger = Logger.getLogger(RealtimePubSamplerUI.class.getCanonicalName());

	private final JLabeledTextField serverAddr = new JLabeledTextField("Environment:");
	private final JLabeledTextField apiKey = new JLabeledTextField("API key");
	public final JLabeledTextField clientIdPrefix = new JLabeledTextField("ClientId:", 8);
	private JCheckBox clientIdSuffix = new JCheckBox("Add random suffix for ClientId");
	final JLabeledTextField channelName = new JLabeledTextField("Channel name:");
	private JCheckBox timestamp = new JCheckBox("Add timestamp in payload");

	JLabeledChoice messageTypes;
	final JSyntaxTextArea sendMessage = JSyntaxTextArea.getInstance(10, 50);
	final JTextScrollPane messagePanel = JTextScrollPane.getInstance(sendMessage);
	final JLabeledTextField stringLength = new JLabeledTextField("Length:");

	public JPanel createConnPanel() {
		JPanel con = new HorizontalPanel();

		JPanel connPanel = new HorizontalPanel();
		connPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Ably connection"));
		connPanel.add(serverAddr);

		con.add(connPanel);
		return con;
	}

	public JPanel createConnOptions() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Client options"));

		JPanel optsPanel0 = new HorizontalPanel();
		optsPanel0.add(clientIdPrefix);
		optsPanel0.add(clientIdSuffix);
		clientIdSuffix.setSelected(true);
		optsPanelCon.add(optsPanel0);

		return optsPanelCon;
	}

	public JPanel createAuthentication() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Authentication"));

		JPanel optsPanel = new HorizontalPanel();
		optsPanel.add(apiKey);
		optsPanelCon.add(optsPanel);

		return optsPanelCon;
	}

	JPanel createPubOption() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Publish options"));

		JPanel optsPanel = new HorizontalPanel();
		optsPanel.add(channelName);
		channelName.setToolTipText("Name of channel that the message will be sent to.");
		optsPanel.add(timestamp);
		optsPanelCon.add(optsPanel);

		return optsPanelCon;
	}

	JPanel createPayload() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Payloads"));

		JPanel horizon1 = new HorizontalPanel();
		messageTypes = new JLabeledChoice("Message type:", new String[] { MESSAGE_TYPE_STRING, MESSAGE_TYPE_HEX_STRING, MESSAGE_TYPE_RANDOM_STR_WITH_FIX_LEN }, false, false);
		messageTypes.addChangeListener(this);
		messageTypes.setSelectedIndex(0);

		horizon1.add(messageTypes, BorderLayout.WEST);
		stringLength.setVisible(false);
		horizon1.add(stringLength);

		JPanel horizon2 = new VerticalPanel();
		messagePanel.setVisible(false);
		horizon2.add(messagePanel);

		optsPanelCon.add(horizon1);
		optsPanelCon.add(horizon2);
		return optsPanelCon;
	}

	@Override
	public void actionPerformed(ActionEvent e) {}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(e.getSource() == messageTypes) {
			int selectedIndex = messageTypes.getSelectedIndex();
			if(selectedIndex == 0 || selectedIndex == 1) {
				stringLength.setVisible(false);
				messagePanel.setVisible(true);
			} else if(selectedIndex == 2) {
				messagePanel.setVisible(false);
				stringLength.setVisible(true);
			} else {
				logger.info("Unknown message type.");
			}
		}
	}

	public void configureClient(AbstractAblySampler sampler) {
		serverAddr.setText(sampler.getEnvironment());
		apiKey.setText(sampler.getApiKey());
		clientIdPrefix.setText(sampler.getClientIdPrefix());
		if(sampler.isClientIdSuffix()) {
			clientIdSuffix.setSelected(true);
		} else {
			clientIdSuffix.setSelected(false);
		}
	}

	public void configurePublisher(AbstractAblySampler sampler) {
		channelName.setText(sampler.getChannel());
		timestamp.setSelected(sampler.isAddTimestamp());
		if(MESSAGE_TYPE_STRING.equalsIgnoreCase(sampler.getMessageType())) {
			messageTypes.setSelectedIndex(0);
			messagePanel.setVisible(true);
		} else if(MESSAGE_TYPE_HEX_STRING.equalsIgnoreCase(sampler.getMessageType())) {
			messageTypes.setSelectedIndex(1);
		} else if(MESSAGE_TYPE_RANDOM_STR_WITH_FIX_LEN.equalsIgnoreCase(sampler.getMessageType())) {
			messageTypes.setSelectedIndex(2);
		}

		stringLength.setText(String.valueOf(sampler.getMessageLength()));
		sendMessage.setText(sampler.getMessage());
	}

	public void setupSamplerClientProperties(AbstractAblySampler sampler) {
		sampler.setEnvironment(serverAddr.getText());
		sampler.setApiKey(apiKey.getText());
		sampler.setClientIdPrefix(clientIdPrefix.getText());
		sampler.setClientIdSuffix(clientIdSuffix.isSelected());
	}

	public void setupSamplerPublishProperties(AbstractAblySampler sampler) {
		sampler.setChannel(channelName.getText());
		sampler.setAddTimestamp(timestamp.isSelected());
		sampler.setMessageType(messageTypes.getText());
		sampler.setMessageLength(stringLength.getText());
		sampler.setMessage(sendMessage.getText());
	}

	public void clearClientUI() {
		serverAddr.setText(DEFAULT_ENVIRONMENT);
		apiKey.setText("");
		clientIdPrefix.setText(DEFAULT_CLIENTID);
		clientIdSuffix.setSelected(true);
	}

	public void clearPublishUI() {
		channelName.setText(DEFAULT_CHANNEL_NAME);
		timestamp.setSelected(false);
		messageTypes.setSelectedIndex(0);
		stringLength.setText(DEFAULT_MESSAGE_FIX_LENGTH);
		sendMessage.setText("");
	}
}
