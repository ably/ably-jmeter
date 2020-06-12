package io.ably.jmeter.gui;

import io.ably.jmeter.Constants;
import io.ably.jmeter.samplers.RestStatsSampler;
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
public class RestStatsSamplerUI extends AbstractSamplerGui implements Constants, ChangeListener {
	private static final long serialVersionUID = 1666890646673145131L;

	final JLabeledTextField start = new JLabeledTextField("Start:");
	final JLabeledTextField end = new JLabeledTextField("End:");
	final JLabeledTextField limit = new JLabeledTextField("Limit:");
	final JLabeledChoice unit = new JLabeledChoice("Unit:", Constants.UNITS.split(","));
	final JLabeledChoice direction = new JLabeledChoice("Direction:", Constants.DIRECTIONS.split(","));

	public RestStatsSamplerUI() {
		this.init();
	}

	private void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);
		JPanel mainPanel = new VerticalPanel();
		add(mainPanel, BorderLayout.CENTER);

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

		JPanel unitPanel = new HorizontalPanel();
		unit.setToolTipText("Select the granularity of the stats request");
		unit.addChangeListener(this);
		unitPanel.add(unit);
		optsPanelCon.add(unitPanel);

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
		RestStatsSampler sampler = (RestStatsSampler)element;
		start.setText(sampler.getStatsStart());
		end.setText(sampler.getStatsEnd());
		limit.setText(sampler.getStatsLimit());
		unit.setSelectedIndex(sampler.getStatsUnitIndex());
		direction.setSelectedIndex(sampler.getStatsDirectionIndex());
	}

	@Override
	public TestElement createTestElement() {
		RestStatsSampler sampler = new RestStatsSampler();
		this.configureTestElement(sampler);
		sampler.setStatsStart(start.getText());
		sampler.setStatsEnd(end.getText());
		sampler.setStatsLimit(limit.getText());
		sampler.setStatsUnitIndex(unit.getSelectedIndex());
		sampler.setStatsDirectionIndex(direction.getSelectedIndex());
		return sampler;
	}

	@Override
	public String getLabelResource() {
		throw new RuntimeException();
	}

	@Override
	public String getStaticLabel() {
		return "Ably REST Stats";
	}

	@Override
	public void modifyTestElement(TestElement arg0) {
		RestStatsSampler sampler = (RestStatsSampler)arg0;
		this.configureTestElement(sampler);
		sampler.setStatsStart(start.getText());
		sampler.setStatsEnd(end.getText());
		sampler.setStatsLimit(limit.getText());
		sampler.setStatsUnitIndex(unit.getSelectedIndex());
		sampler.setStatsDirectionIndex(direction.getSelectedIndex());
	}

	@Override
	public void clearGui() {
		super.clearGui();
		start.setText(DEFAULT_STATS_START);
		end.setText(DEFAULT_STATS_END);
		limit.setText(DEFAULT_STATS_LIMIT);
		unit.setSelectedIndex(DEFAULT_UNIT);
		direction.setSelectedIndex(DEFAULT_STATS_DIRECTION);
	}

	@Override
	public void stateChanged(ChangeEvent e) {}
}
