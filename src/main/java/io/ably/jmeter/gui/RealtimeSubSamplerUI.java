package io.ably.jmeter.gui;

import io.ably.jmeter.Constants;
import io.ably.jmeter.samplers.RealtimeSubSampler;
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
import java.util.logging.Logger;

/**
 * The GUI for the realtime subscribe sampler
 */
public class RealtimeSubSamplerUI extends AbstractSamplerGui implements Constants, ChangeListener{
	private static final long serialVersionUID = 1715399546099472610L;
	private static final Logger logger = Logger.getLogger(RealtimeSubSamplerUI.class.getCanonicalName());

	private JLabeledChoice sampleOnCondition;
	private final JLabeledTextField sampleConditionValue = new JLabeledTextField("");
	private final JLabeledTextField channelName = new JLabeledTextField("Channel name:");
	private JCheckBox debugResponse = new JCheckBox("Debug response");
	private JCheckBox timestamp = new JCheckBox("Payload includes timestamp");

	public RealtimeSubSamplerUI() {
		this.init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);
		JPanel mainPanel = new VerticalPanel();
		add(mainPanel, BorderLayout.CENTER);

		mainPanel.add(createSubOption());
	}
	
	private JPanel createSubOption() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Sub options"));

		sampleOnCondition = new JLabeledChoice("Sample on:", new String[] {SAMPLE_ON_CONDITION_OPTION1, SAMPLE_ON_CONDITION_OPTION2});

		JPanel optsPanel1 = new HorizontalPanel();
		optsPanel1.add(channelName);
		channelName.setToolTipText("Channel to subscribe on");
		optsPanel1.add(timestamp);
		optsPanelCon.add(optsPanel1);
		
		JPanel optsPanel3 = new HorizontalPanel();
		sampleOnCondition.addChangeListener(this);
		optsPanel3.add(sampleOnCondition);
		optsPanel3.add(sampleConditionValue);
		sampleOnCondition.setToolTipText("When sub sampler should report out");
		sampleConditionValue.setToolTipText("Please specify an integer value great than 0, other values will be ignored");
		optsPanelCon.add(optsPanel3);
		
		JPanel optsPanel2 = new HorizontalPanel();
		optsPanel2.add(debugResponse);
		optsPanelCon.add(optsPanel2);

		return optsPanelCon;
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

		this.channelName.setText(sampler.getChannel());
		this.timestamp.setSelected(sampler.isAddTimestamp());
		this.debugResponse.setSelected(sampler.isDebugResponse());
		this.sampleOnCondition.setText(sampler.getSampleCondition());

		if(SAMPLE_ON_CONDITION_OPTION1.equalsIgnoreCase(sampleOnCondition.getText())) {
			this.sampleConditionValue.setText(sampler.getSampleElapsedTime());
		} else if(SAMPLE_ON_CONDITION_OPTION2.equalsIgnoreCase(sampleOnCondition.getText())) {
			this.sampleConditionValue.setText(sampler.getSampleCount());
		}
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
		sampler.setChannel(this.channelName.getText());
		sampler.setAddTimestamp(this.timestamp.isSelected());
		sampler.setDebugResponse(this.debugResponse.isSelected());
		sampler.setSampleCondition(this.sampleOnCondition.getText());
		
		if(SAMPLE_ON_CONDITION_OPTION1.equalsIgnoreCase(sampleOnCondition.getText())) {
			sampler.setSampleElapsedTime(this.sampleConditionValue.getText());
		} else if(SAMPLE_ON_CONDITION_OPTION2.equalsIgnoreCase(sampleOnCondition.getText())) {
			sampler.setSampleCount(this.sampleConditionValue.getText());
		}
	}
	
	@Override
	public void clearGui() {
		super.clearGui();
		this.channelName.setText(DEFAULT_CHANNEL_NAME);
		this.timestamp.setSelected(false);
		this.debugResponse.setSelected(false);
		this.sampleOnCondition.setText(SAMPLE_ON_CONDITION_OPTION1);
		this.sampleConditionValue.setText(DEFAULT_SAMPLE_VALUE_ELAPSED_TIME_MILLI_SEC);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(this.sampleOnCondition == e.getSource()) {
			if(SAMPLE_ON_CONDITION_OPTION1.equalsIgnoreCase(sampleOnCondition.getText())) {
				sampleConditionValue.setText(DEFAULT_SAMPLE_VALUE_ELAPSED_TIME_MILLI_SEC);
			} else if(SAMPLE_ON_CONDITION_OPTION2.equalsIgnoreCase(sampleOnCondition.getText())) {
				sampleConditionValue.setText(DEFAULT_SAMPLE_VALUE_COUNT);
			}
		}
	}
}
