package eu.fp7.scase.servicecomposition.views;

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

import eu.fp7.scase.servicecomposition.importer.Importer.Argument;
import eu.fp7.scase.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;

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
			if (data.getisMatchedIO()){
				node.setBackgroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE));
			}else{
			node.setBackgroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
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
			if (((OwlService)(((MyNode) node.getData()).getObject())).getContent() instanceof Argument) {

				Argument arg =(Argument)((OwlService)(((MyNode) node.getData()).getObject())).getContent();

				tooltip.add(new Label("I/O: " + arg.getName()));
			}
			node.setTooltip(tooltip);
		} else if (data.getType().equals("Condition")) {
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
			// create tooltip

			IFigure tooltip = new Figure();
			tooltip.setBorder(new MarginBorder(5, 5, 5, 5));
			FlowLayout layout = new FlowLayout(false);
			layout.setMajorSpacing(3);
			layout.setMinorAlignment(3);
			tooltip.setLayoutManager(new FlowLayout(false));

			tooltip.add(new Label("Action: " + node.getText()));
			node.setTooltip(tooltip);
		}

	}

}
