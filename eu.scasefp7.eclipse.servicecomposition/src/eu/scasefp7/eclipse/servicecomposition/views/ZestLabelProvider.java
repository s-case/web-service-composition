package eu.scasefp7.eclipse.servicecomposition.views;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.eclipse.zest.core.viewers.ISelfStyleProvider;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;

import eu.scasefp7.eclipse.servicecomposition.Activator;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;

public class ZestLabelProvider extends LabelProvider implements ISelfStyleProvider {
	@Override
	public String getText(Object element) {
		if (element instanceof MyNode) {
			MyNode myNode = (MyNode) element;
			return myNode.getName();
		}
		// Not called with the IGraphEntityContentProvider
		if (element instanceof MyConnection) {
			MyConnection myConnection = (MyConnection) element;
			return myConnection.getLabel();
		}

		if (element instanceof EntityConnectionData) {

			return "";
		}
		throw new RuntimeException("Wrong type: " + element.getClass().toString());
	}

	@Override
	public void selfStyleConnection(Object element, GraphConnection connection) {
		if (element instanceof EntityConnectionData) {
			EntityConnectionData connectionData = (EntityConnectionData) element;

			MyNode dest = (MyNode) connectionData.dest;
			MyNode source = (MyNode) connectionData.source;
			for (int i = 0; i < source.getLinkedConnections().size(); i++) {
				MyConnection tmp = source.getLinkedConnections().get(i);

				if (tmp.getDestination().equals(dest)) {
					connection.setText(tmp.getLabel());
				}
			}

		}

	}

	@Override
	public void selfStyleNode(Object element, GraphNode node) {
		MyNode node1 = (MyNode) node.getData();
		OwlService data = (OwlService) node1.getObject();
		// set color of nodes
		if (data.getType().equals("StartNode")) {
			if (Activator.getDefault() != null) {
				String colorString = Activator.getDefault().getPreferenceStore().getString("Start Node");
				int r = Integer.parseInt(colorString.split(",")[0]);
				int g = Integer.parseInt(colorString.split(",")[1]);
				int b = Integer.parseInt(colorString.split(",")[2]);
				node.setBackgroundColor(new Color(Display.getCurrent(), r, g, b));
			} else
				node.setBackgroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
			node.setForegroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			node.setHighlightColor(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW));
			// create tooltip

			IFigure tooltip = new Figure();
			tooltip.setBorder(new MarginBorder(5, 5, 5, 5));
			FlowLayout layout = new FlowLayout(false);
			layout.setMajorSpacing(3);
			layout.setMinorAlignment(3);
			tooltip.setLayoutManager(new FlowLayout(false));

			tooltip.add(new Label("Start node of workflow"));
			node.setTooltip(tooltip);

		} else if (data.getType().equals("EndNode")) {
			
			if (Activator.getDefault() != null) {
				String colorString = Activator.getDefault().getPreferenceStore().getString("End Node");
				int r = Integer.parseInt(colorString.split(",")[0]);
				int g = Integer.parseInt(colorString.split(",")[1]);
				int b = Integer.parseInt(colorString.split(",")[2]);
				node.setBackgroundColor(new Color(Display.getCurrent(), r, g, b));
			} else
				node.setBackgroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
			node.setForegroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			node.setHighlightColor(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW));
			// create tooltip

			IFigure tooltip = new Figure();
			tooltip.setBorder(new MarginBorder(5, 5, 5, 5));
			FlowLayout layout = new FlowLayout(false);
			layout.setMajorSpacing(3);
			layout.setMinorAlignment(3);
			tooltip.setLayoutManager(new FlowLayout(false));

			tooltip.add(new Label("End node of workflow"));
			node.setTooltip(tooltip);
		} else if (data.getType().equals("Property")) {
			if (data.getisMatchedIO()) {
				if (Activator.getDefault() != null) {
					String colorString = Activator.getDefault().getPreferenceStore().getString("Matched Input/Output");
					int r = Integer.parseInt(colorString.split(",")[0]);
					int g = Integer.parseInt(colorString.split(",")[1]);
					int b = Integer.parseInt(colorString.split(",")[2]);
					node.setBackgroundColor(new Color(Display.getCurrent(), r, g, b));
				} else
					node.setBackgroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE));
				
				
			} else {
				if (Activator.getDefault() != null) {
					String colorString = Activator.getDefault().getPreferenceStore().getString("Input/Output");
					int r = Integer.parseInt(colorString.split(",")[0]);
					int g = Integer.parseInt(colorString.split(",")[1]);
					int b = Integer.parseInt(colorString.split(",")[2]);
					node.setBackgroundColor(new Color(Display.getCurrent(), r, g, b));
				} else
					node.setBackgroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_MAGENTA));
				
			}

			node.setForegroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			node.setHighlightColor(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW));
			// create tooltip

			IFigure tooltip = new Figure();
			tooltip.setBorder(new MarginBorder(5, 5, 5, 5));
			FlowLayout layout = new FlowLayout(false);
			layout.setMajorSpacing(3);
			layout.setMinorAlignment(3);
			tooltip.setLayoutManager(new FlowLayout(false));
			if (((OwlService) (((MyNode) node.getData()).getObject())).getContent() instanceof Argument) {

				Argument arg = (Argument) ((OwlService) (((MyNode) node.getData()).getObject())).getContent();

				tooltip.add(new Label("I/O: " + arg.getName()));
			}
			node.setTooltip(tooltip);
		} else if (data.getType().equals("Condition")) {
			
			
			if (Activator.getDefault() != null) {
				String colorString = Activator.getDefault().getPreferenceStore().getString("Condition");
				int r = Integer.parseInt(colorString.split(",")[0]);
				int g = Integer.parseInt(colorString.split(",")[1]);
				int b = Integer.parseInt(colorString.split(",")[2]);
				node.setBackgroundColor(new Color(Display.getCurrent(), r, g, b));
			} else
				node.setBackgroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_CYAN));
			
			
			// create tooltip

			IFigure tooltip = new Figure();
			tooltip.setBorder(new MarginBorder(5, 5, 5, 5));
			FlowLayout layout = new FlowLayout(false);
			layout.setMajorSpacing(3);
			layout.setMinorAlignment(3);
			tooltip.setLayoutManager(new FlowLayout(false));

			tooltip.add(new Label("Condition: " + node.getText()));
			node.setTooltip(tooltip);
		} else if (data.getType().equals("Action")) {
			if (Activator.getDefault() != null) {
				String colorString = Activator.getDefault().getPreferenceStore().getString("Operation");
				int r = Integer.parseInt(colorString.split(",")[0]);
				int g = Integer.parseInt(colorString.split(",")[1]);
				int b = Integer.parseInt(colorString.split(",")[2]);
				node.setBackgroundColor(new Color(Display.getCurrent(), r, g, b));
			} else
				node.setBackgroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
			// create tooltip

			IFigure tooltip = new Figure();
			tooltip.setBorder(new MarginBorder(5, 5, 5, 5));
			FlowLayout layout = new FlowLayout(false);
			layout.setMajorSpacing(3);
			layout.setMinorAlignment(3);
			tooltip.setLayoutManager(new FlowLayout(false));
			if (((OwlService) ((MyNode) node.getData()).getObject()).getOperation().getDomain() != null) {
				if (((OwlService) ((MyNode) node.getData()).getObject()).getOperation().getDescription() != null) {
					String uri = ((OwlService) ((MyNode) node.getData()).getObject()).getOperation().getDomain()
							.getURI();
					if (uri.contains("109.231.122.51")){
						uri = uri.replace("109.231.122.51", "wsc.scasefp7.com");
					}
					tooltip.add(
							new Label("Operation: " + node.getText() + "\nBase URI: "
									+ uri
									+ "\nDescription: " + ((OwlService) ((MyNode) node.getData()).getObject())
											.getOperation().getDescription()));
				} else {
					tooltip.add(new Label("Operation: " + node.getText() + "\nBase URI: "
							+ ((OwlService) ((MyNode) node.getData()).getObject()).getOperation().getDomain()
									.getURI()));
				}

			}
			node.setTooltip(tooltip);
		}

	}

}
