package io.ably.jmeter.gui;

import io.ably.jmeter.AblyLog;
import io.ably.jmeter.Constants;
import io.ably.jmeter.Util;
import io.ably.jmeter.samplers.AbstractAblySampler;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Common code for UI elements used across multiple sampler GUIs
 */
public class CommonUIElements implements ChangeListener, ActionListener, Constants {
	private static final Logger logger = LoggerFactory.getLogger(CommonUIElements.class.getCanonicalName());

	private final JLabeledTextField serverAddr = new JLabeledTextField("Environment:");
	private final JLabeledTextField apiKey = new JLabeledTextField("API key");
	public final JLabeledTextField clientIdPrefix = new JLabeledTextField("ClientId:", 8);
	private JCheckBox clientIdSuffix = new JCheckBox("Add random suffix for clientId");
	private JLabeledChoice logLevel = new JLabeledChoice("Log level", AblyLog.levelNames);

	final JLabeledTextField channelNamePrefix = new JLabeledTextField("Channel name:");
	private JCheckBox channelNameSuffix = new JCheckBox("Add random suffix for channel name");

	final JLabeledTextField eventName = new JLabeledTextField("Event name:");
	final JLabeledTextField encoding = new JLabeledTextField("Encoding:");

	final ArgumentsPanel channelParams = new ArgumentsPanel("Params");
	private JCheckBox timestamp = new JCheckBox("Add timestamp in message metadata");

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

	public JPanel createClientOptions() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Client options"));

		JPanel optsPanel0 = new HorizontalPanel();
		optsPanel0.add(clientIdPrefix);
		optsPanel0.add(clientIdSuffix);
		clientIdSuffix.setSelected(true);
		optsPanelCon.add(optsPanel0);

		return optsPanelCon;
	}

	public JPanel createLibraryOptions() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Library options"));
		optsPanelCon.add(logLevel);

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
		optsPanel.add(channelNamePrefix);
		channelNamePrefix.setToolTipText("Name of channel that the message will be sent to.");
		optsPanel.add(channelNameSuffix);
		channelNameSuffix.setSelected(false);
		optsPanelCon.add(optsPanel);

		return optsPanelCon;
	}

	JPanel createMessagePayload() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Payload"));

		JPanel horizon1 = new HorizontalPanel();
		messageTypes = new JLabeledChoice("Payload type:", new String[] { MESSAGE_TYPE_STRING, MESSAGE_TYPE_HEX_STRING, MESSAGE_TYPE_RANDOM_STR_WITH_FIX_LEN }, false, false);
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

	JPanel createMessageAttributes() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Message attributes"));

		JPanel attrsPanel = new VerticalPanel();
		attrsPanel.add(eventName);
		attrsPanel.add(encoding);
		attrsPanel.add(timestamp);
		optsPanelCon.add(attrsPanel);

		return optsPanelCon;
	}

	JPanel createRequestParams() {
		JPanel paramsPanelCon = new VerticalPanel();
		paramsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Request params"));
		paramsPanelCon.add(channelParams);

		return paramsPanelCon;
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
		logLevel.setSelectedIndex(sampler.getLogLevelIndex());
	}

	public void configurePublisher(AbstractAblySampler sampler) {
		channelNamePrefix.setText(sampler.getChannelPrefix());
		if(sampler.isChannelNameSuffix()) {
			channelNameSuffix.setSelected(true);
		} else {
			channelNameSuffix.setSelected(false);
		}
		eventName.setText(sampler.getMessageEventName());
		encoding.setText(sampler.getMessageEncoding());
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

	public void configureParams(AbstractAblySampler sampler) {
		channelParams.configure(Util.mapToArguments(sampler.getChannelParams()));
	}

	public void setupSamplerClientProperties(AbstractAblySampler sampler) {
		sampler.setEnvironment(serverAddr.getText());
		sampler.setApiKey(apiKey.getText());
		sampler.setClientIdPrefix(clientIdPrefix.getText());
		sampler.setClientIdSuffix(clientIdSuffix.isSelected());
		sampler.setLogLevelIndex(logLevel.getSelectedIndex());
	}

	public void setupSamplerPublishProperties(AbstractAblySampler sampler) {
		sampler.setChannelPrefix(channelNamePrefix.getText());
		sampler.setChannelNameSuffix(channelNameSuffix.isSelected());
		sampler.setMessageEventName(eventName.getText());
		sampler.setMessageEncoding(encoding.getText());
		sampler.setAddTimestamp(timestamp.isSelected());
		sampler.setMessageType(messageTypes.getText());
		sampler.setMessageLength(stringLength.getText());
		sampler.setMessage(sendMessage.getText());
	}

	public void setupSamplerParamsProperties(AbstractAblySampler sampler) {
		sampler.setChannelParams(Util.argumentsToMap((Arguments)channelParams.createTestElement()));
	}

	public void clearClientUI() {
		serverAddr.setText(DEFAULT_ENVIRONMENT);
		apiKey.setText(DEFAULT_API_KEY);
		clientIdPrefix.setText(DEFAULT_CLIENTID);
		clientIdSuffix.setSelected(true);
		logLevel.setSelectedIndex(DEFAULT_LOG_LEVEL);
	}

	public void clearPublishUI() {
		channelNamePrefix.setText(DEFAULT_CHANNEL_NAME_PREFIX);
		channelNameSuffix.setSelected(false);
		eventName.setText(DEFAULT_EVENT_NAME);
		encoding.setText(DEFAULT_ENCODING);
		timestamp.setSelected(false);
		messageTypes.setSelectedIndex(0);
		stringLength.setText(DEFAULT_MESSAGE_FIX_LENGTH);
		sendMessage.setText("");
	}

	public void clearParamsUI() {
		channelParams.clear();
	}
}
