/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import com.cburch.draw.toolbar.Toolbar;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitEvent;
import com.cburch.logisim.circuit.CircuitListener;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.AttributeSets;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.file.LibraryEvent;
import com.cburch.logisim.file.LibraryListener;
import com.cburch.logisim.gui.appear.AppearanceView;
import com.cburch.logisim.gui.generic.AttributeTable;
import com.cburch.logisim.gui.generic.AttributeTableListener;
import com.cburch.logisim.gui.generic.BasicZoomModel;
import com.cburch.logisim.gui.generic.CanvasPane;
import com.cburch.logisim.gui.generic.CardPanel;
import com.cburch.logisim.gui.generic.LFrame;
import com.cburch.logisim.gui.generic.ZoomControl;
import com.cburch.logisim.gui.generic.ZoomModel;
import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectActions;
import com.cburch.logisim.proj.ProjectEvent;
import com.cburch.logisim.proj.ProjectListener;
import com.cburch.logisim.proj.Projects;
import com.cburch.logisim.tools.SetAttributeAction;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.util.HorizontalSplitPane;
import com.cburch.logisim.util.LocaleListener;
import com.cburch.logisim.util.LocaleManager;
import com.cburch.logisim.util.StringUtil;
import com.cburch.logisim.util.VerticalSplitPane;

public class Frame extends LFrame implements LocaleListener {
	public static final String LAYOUT = "layout";
	public static final String APPEARANCE = "appearance";

	private static final double[] ZOOM_OPTIONS = { 20, 50, 75, 100, 133, 150, 200 };
	
	class MyProjectListener
			implements ProjectListener, LibraryListener, CircuitListener,
				PropertyChangeListener {
		public void projectChanged(ProjectEvent event) {
			int action = event.getAction();

			if (action == ProjectEvent.ACTION_SET_FILE) {
				computeTitle();
				proj.setTool(proj.getOptions().getToolbarData().getFirstTool());
				placeToolbar();
			} else if (action == ProjectEvent.ACTION_SET_CURRENT) {
				setView(LAYOUT);
				if (appearance != null) {
					appearance.setCircuit(proj, proj.getCircuitState());
				}
				viewAttributes(proj.getTool());
				computeTitle();
			} else if (action == ProjectEvent.ACTION_SET_TOOL) {
				if (attrTable == null) return; // for startup
				Tool oldTool = (Tool) event.getOldData();
				Tool newTool = (Tool) event.getData();
				viewAttributes(oldTool, newTool, false);
			}
		}

		public void libraryChanged(LibraryEvent e) {
			if (e.getAction() == LibraryEvent.SET_NAME) {
				computeTitle();
			} else if (e.getAction() == LibraryEvent.DIRTY_STATE) {
				enableSave();
			}
		}

		public void circuitChanged(CircuitEvent event) {
			if (event.getAction() == CircuitEvent.ACTION_SET_NAME) {
				computeTitle();
			}
		}

		private void enableSave() {
			Project proj = getProject();
			boolean ok = proj.isFileDirty();
			getRootPane().putClientProperty("windowModified", Boolean.valueOf(ok));
		}

		public void attributeListChanged(AttributeEvent e) { }

		public void propertyChange(PropertyChangeEvent event) {
			if (AppPreferences.SHOW_PROJECT_TOOLBAR.isSource(event)) {
				boolean val = ((Boolean) event.getNewValue()).booleanValue();
				projectToolbar.setVisible(val);
			} else if (AppPreferences.TOOLBAR_PLACEMENT.isSource(event)) {
				placeToolbar();
			}
		}
	}

	class MyWindowListener extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			if (confirmClose(Strings.get("confirmCloseTitle"))) {
				layoutCanvas.closeCanvas();
				Frame.this.dispose();
			}
		}

		@Override
		public void windowOpened(WindowEvent e) {
			layoutCanvas.computeSize(true);
		}
	}

	private static class ComponentAttributeListener
			implements AttributeTableListener {
		Project proj;
		Circuit circ;
		Component comp;

		ComponentAttributeListener(Project proj, Circuit circ,
				Component comp) {
			this.proj = proj;
			this.circ = circ;
			this.comp = comp;
		}

		public void valueChangeRequested(AttributeTable table,
				AttributeSet attrs, Attribute<?> attr, Object value) {
			if (!proj.getLogisimFile().contains(circ)) {
				JOptionPane.showMessageDialog(proj.getFrame(),
					Strings.get("cannotModifyCircuitError"));
			} else {
				SetAttributeAction act = new SetAttributeAction(circ,
						Strings.getter("changeAttributeAction"));
				act.set(comp, attr, value);
				proj.doAction(act);
			}
		}
	}
	
	private Project         proj;
	private MyProjectListener myProjectListener = new MyProjectListener();

	// GUI elements shared between views
	private LogisimMenuBar  menubar;
	private MenuListener    menuListener;
	private Toolbar         toolbar;
	private HorizontalSplitPane leftRegion;
	private VerticalSplitPane mainRegion;
	private JPanel          mainPanelSuper;
	private CardPanel       mainPanel;
	// left-side elements
	private Toolbar         projectToolbar;
	private ProjectToolbarModel projectToolbarModel;
	private Explorer        explorer;
	private AttributeTable  attrTable;
	private ZoomControl     zoom;
	
	// for the Layout view
	private LayoutToolbarModel layoutToolbarModel;
	private Canvas          layoutCanvas;
	private ZoomModel       layoutZoomModel;
	private LayoutEditHandler layoutEditHandler;
	
	// for the Appearance view
	private AppearanceView appearance;

	public Frame(Project proj) {
		this.proj = proj;
		proj.setFrame(this);

		setBackground(Color.white);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new MyWindowListener());

		proj.addProjectListener(myProjectListener);
		proj.addLibraryListener(myProjectListener);
		proj.addCircuitListener(myProjectListener);
		computeTitle();
		
		// set up elements for the Layout view
		layoutToolbarModel = new LayoutToolbarModel(this, proj);
		layoutCanvas = new Canvas(proj);
		layoutZoomModel = new BasicZoomModel(AppPreferences.LAYOUT_SHOW_GRID,
				AppPreferences.LAYOUT_ZOOM, ZOOM_OPTIONS);

		layoutCanvas.getGridPainter().setZoomModel(layoutZoomModel);
		layoutEditHandler = new LayoutEditHandler(this);

		// set up menu bar and toolbar
		menubar = new LogisimMenuBar(this, proj);
		setJMenuBar(menubar);
		toolbar = new Toolbar(layoutToolbarModel);

		// set up the left-side components
		projectToolbarModel = new ProjectToolbarModel(this);
		projectToolbar = new Toolbar(projectToolbarModel);
		projectToolbar.setVisible(AppPreferences.SHOW_PROJECT_TOOLBAR.getBoolean());
		explorer = new Explorer(proj);
		explorer.setListener(new ExplorerManip(proj, explorer));
		attrTable = new AttributeTable(this);
		zoom = new ZoomControl(layoutZoomModel);

		// set up the central area
		CanvasPane canvasPane = new CanvasPane(layoutCanvas);
		mainPanelSuper = new JPanel(new BorderLayout());
		canvasPane.setZoomModel(layoutZoomModel);
		mainPanel = new CardPanel();
		mainPanel.addView(LAYOUT, canvasPane);
		mainPanel.setView(LAYOUT);
		mainPanelSuper.add(mainPanel, BorderLayout.CENTER);

		// now register the menu listener
		menuListener = new MenuListener(this, menubar, projectToolbarModel);
		menuListener.setEditHandler(layoutEditHandler);

		// set up the contents, split down the middle, with the canvas
		// on the right and a split pane on the left containing the
		// explorer and attribute values.
		JPanel explPanel = new JPanel(new BorderLayout());
		explPanel.add(projectToolbar, BorderLayout.NORTH);
		explPanel.add(new JScrollPane(explorer), BorderLayout.CENTER);
		JPanel attrPanel = new JPanel(new BorderLayout());
		attrPanel.add(new JScrollPane(attrTable), BorderLayout.CENTER);
		attrPanel.add(zoom, BorderLayout.SOUTH);

		leftRegion = new HorizontalSplitPane(explPanel, attrPanel,
				AppPreferences.WINDOW_LEFT_SPLIT.get().doubleValue());
		mainRegion = new VerticalSplitPane(leftRegion, mainPanelSuper,
				AppPreferences.WINDOW_MAIN_SPLIT.get().doubleValue());

		getContentPane().add(mainRegion, BorderLayout.CENTER);

		computeTitle();

		this.setSize(AppPreferences.WINDOW_WIDTH.get().intValue(),
				AppPreferences.WINDOW_HEIGHT.get().intValue());
		Point prefPoint = getInitialLocation();
		if (prefPoint != null) {
			this.setLocation(prefPoint);
		}
		this.setExtendedState(AppPreferences.WINDOW_STATE.get().intValue());
		
		menuListener.register(mainPanel);
		KeyboardToolSelection.register(toolbar);

		if (proj.getTool() == null) {
			proj.setTool(proj.getOptions().getToolbarData().getFirstTool());
		}
		AppPreferences.SHOW_PROJECT_TOOLBAR.addPropertyChangeListener(myProjectListener);
		AppPreferences.TOOLBAR_PLACEMENT.addPropertyChangeListener(myProjectListener);
		placeToolbar();

		LocaleManager.addLocaleListener(this);
	}
	
	private void placeToolbar() {
		String loc = AppPreferences.TOOLBAR_PLACEMENT.get();
		Container contents = getContentPane();
		contents.remove(toolbar);
		mainPanelSuper.remove(toolbar);
		if (AppPreferences.TOOLBAR_HIDDEN.equals(loc)) {
			; // don't place value anywhere
		} else if (AppPreferences.TOOLBAR_DOWN_MIDDLE.equals(loc)) {
			toolbar.setOrientation(Toolbar.VERTICAL);
			mainPanelSuper.add(toolbar, BorderLayout.WEST);
		} else { // it is a BorderLayout constant
			Object value = BorderLayout.NORTH;
			for (Direction dir : Direction.cardinals) {
				if (dir.toString().equals(loc)) {
					if (dir == Direction.EAST)       value = BorderLayout.EAST;
					else if (dir == Direction.SOUTH) value = BorderLayout.SOUTH;
					else if (dir == Direction.WEST)  value = BorderLayout.WEST;
					else                             value = BorderLayout.NORTH;
				}
			}

			contents.add(toolbar, value);
			boolean vertical = value == BorderLayout.WEST || value == BorderLayout.EAST;
			toolbar.setOrientation(vertical ? Toolbar.VERTICAL : Toolbar.HORIZONTAL);
		}
		contents.validate();
	}
	
	public Project getProject() {
		return proj;
	}

	public void viewComponentAttributes(Circuit circ, Component comp) {
		if (comp == null) {
			attrTable.setAttributeSet(null, null);
			layoutCanvas.setHaloedComponent(null, null);
		} else {
			attrTable.setAttributeSet(comp.getAttributeSet(),
				new ComponentAttributeListener(proj, circ, comp));
			layoutCanvas.setHaloedComponent(circ, comp);
		}
		layoutToolbarModel.setHaloedTool(null);
		explorer.setHaloedTool(null);
	}

	public AttributeTable getAttributeTable() {
		return attrTable;
	}
	
	public String getView() {
		return mainPanel.getView();
	}
	
	public void setView(String view) {
		String curView = mainPanel.getView();
		if (curView.equals(view)) return;
		
		if (view.equals(APPEARANCE)) { // appearance view
			AppearanceView app = appearance;
			if (app == null) {
				app = new AppearanceView();
				app.setCircuit(proj, proj.getCircuitState());
				mainPanel.addView(APPEARANCE, app.getCanvasPane());
				appearance = app;
			}
			toolbar.setToolbarModel(app.getToolbarModel());
			attrTable.setAttributeSet(app.getAttributeSet(),
					app.getAttributeManager(attrTable));
			app.getAttributeManager(attrTable).attributesSelected();
			zoom.setZoomModel(app.getZoomModel());
			menuListener.setEditHandler(app.getEditHandler());
			mainPanel.setView(view);
			app.getCanvas().requestFocus();
		} else { // layout view
			toolbar.setToolbarModel(layoutToolbarModel);
			zoom.setZoomModel(layoutZoomModel);
			menuListener.setEditHandler(layoutEditHandler);
			viewAttributes(proj.getTool(), true);
			mainPanel.setView(view);
			layoutCanvas.requestFocus();
		}
	}

	public Canvas getCanvas() {
		return layoutCanvas;
	}

	private void computeTitle() {
		String s;
		Circuit circuit = proj.getCurrentCircuit();
		String name = proj.getLogisimFile().getName();
		if (circuit != null) {
			s = StringUtil.format(Strings.get("titleCircFileKnown"),
				circuit.getName(), name);
		} else {
			s = StringUtil.format(Strings.get("titleFileKnown"), name);
		}
		this.setTitle(s);
		myProjectListener.enableSave();
	}
	
	void viewAttributes(Tool newTool) {
		viewAttributes(null, newTool, false);
	}
	
	private void viewAttributes(Tool newTool, boolean force) {
		viewAttributes(null, newTool, force);
	}

	private void viewAttributes(Tool oldTool, Tool newTool, boolean force) {
		AttributeSet newAttrs = null;
		if (newTool == null) {
			if (!force) return;
		} else {
			newAttrs = newTool.getAttributeSet();
		}
		if (newAttrs == null) {
			AttributeSet oldAttrs = oldTool == null ? null : oldTool.getAttributeSet();
			AttributeTableListener listen = attrTable.getAttributeTableListener();
			if (!force && attrTable.getAttributeSet() != oldAttrs
					&& !(listen instanceof CircuitAttributeListener)) {
				return;
			}
		}
		if (newAttrs == null) {
			Circuit circ = proj.getCurrentCircuit();
			if (circ != null) {
				attrTable.setAttributeSet(circ.getStaticAttributes(),
						new CircuitAttributeListener(proj, circ));
			} else if (force) {
				attrTable.setAttributeSet(AttributeSets.EMPTY, null);
			}
		} else {
			attrTable.setAttributeSet(newAttrs, newTool.getAttributeTableListener(proj));
		}
		if (newAttrs != null && newAttrs.getAttributes().size() > 0) {
			layoutToolbarModel.setHaloedTool(newTool);
			explorer.setHaloedTool(newTool);
		} else {
			layoutToolbarModel.setHaloedTool(null);
			explorer.setHaloedTool(null);
		}
		layoutCanvas.setHaloedComponent(null, null);
	}

	public void localeChanged() {
		computeTitle();
	}
	
	public void savePreferences() {
		AppPreferences.TICK_FREQUENCY.set(Double.valueOf(proj.getSimulator().getTickFrequency()));
		AppPreferences.LAYOUT_SHOW_GRID.setBoolean(layoutZoomModel.getShowGrid());
		AppPreferences.LAYOUT_ZOOM.set(Double.valueOf(layoutZoomModel.getZoomFactor()));
		if (appearance != null) {
			ZoomModel aZoom = appearance.getZoomModel();
			AppPreferences.APPEARANCE_SHOW_GRID.setBoolean(aZoom.getShowGrid());
			AppPreferences.APPEARANCE_ZOOM.set(Double.valueOf(aZoom.getZoomFactor()));
		}
		int state = getExtendedState() & ~JFrame.ICONIFIED;
		AppPreferences.WINDOW_STATE.set(Integer.valueOf(state));
		Dimension dim = getSize();
		AppPreferences.WINDOW_WIDTH.set(Integer.valueOf(dim.width));
		AppPreferences.WINDOW_HEIGHT.set(Integer.valueOf(dim.height));
		Point loc;
		try {
			loc = getLocationOnScreen();
		} catch (IllegalComponentStateException e) {
			loc = Projects.getLocation(this);
		}
		if (loc != null) {
			AppPreferences.WINDOW_LOCATION.set(loc.x + "," + loc.y);
		}
		AppPreferences.WINDOW_LEFT_SPLIT.set(Double.valueOf(leftRegion.getFraction()));
		AppPreferences.WINDOW_MAIN_SPLIT.set(Double.valueOf(mainRegion.getFraction()));
	}
	
	public boolean confirmClose() {
		return confirmClose(Strings.get("confirmCloseTitle"));
	}
	
	// returns true if user is OK with proceeding
	public boolean confirmClose(String title) {
		String message = StringUtil.format(Strings.get("confirmDiscardMessage"),
				proj.getLogisimFile().getName());
		
		if (!proj.isFileDirty()) return true;
		toFront();
		String[] options = { Strings.get("saveOption"), Strings.get("discardOption"), Strings.get("cancelOption") };
		int result = JOptionPane.showOptionDialog(this,
				message, title, 0, JOptionPane.QUESTION_MESSAGE, null,
				options, options[0]);
		boolean ret;
		if (result == 0) {
			ret = ProjectActions.doSave(proj);
		} else if (result == 1) {
			ret = true;
		} else {
			ret = false;
		}
		if (ret) {
			dispose();
		}
		return ret;
	}
	
	private static Point getInitialLocation() {
		String s = AppPreferences.WINDOW_LOCATION.get();
		if (s == null) return null;
		int comma = s.indexOf(',');
		if (comma < 0) return null;
		try {
			int x = Integer.parseInt(s.substring(0, comma));
			int y = Integer.parseInt(s.substring(comma + 1));
			while (isProjectFrameAt(x, y)) {
				x += 20;
				y += 20;
			}
			Rectangle desired = new Rectangle(x, y, 50, 50);
		
			int gcBestSize = 0;
			Point gcBestPoint = null;
			GraphicsEnvironment ge;
			ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			for (GraphicsDevice gd : ge.getScreenDevices()) {
				for (GraphicsConfiguration gc : gd.getConfigurations()) {
					Rectangle gcBounds = gc.getBounds();
					if (gcBounds.intersects(desired)) {
						Rectangle inter = gcBounds.intersection(desired);
						int size = inter.width * inter.height;
						if (size > gcBestSize) {
							gcBestSize = size;
							int x2 = Math.max(gcBounds.x, Math.min(inter.x,
									inter.x + inter.width - 50));
							int y2 = Math.max(gcBounds.y, Math.min(inter.y,
									inter.y + inter.height - 50));
							gcBestPoint = new Point(x2, y2);
						}
					}
				}
			}
			if (gcBestPoint != null) {
				if (isProjectFrameAt(gcBestPoint.x, gcBestPoint.y)) {
					gcBestPoint = null;
				}
			}
			return gcBestPoint;
		} catch (Throwable t) {
			return null;
		}
	}

	private static boolean isProjectFrameAt(int x, int y) {
		for (Project current : Projects.getOpenProjects()) {
			Frame frame = current.getFrame();
			if (frame != null) {
				Point loc = frame.getLocationOnScreen();
				int d = Math.abs(loc.x - x) + Math.abs(loc.y - y);
				if (d <= 3) return true;
			}
		}
		return false;
	}
}