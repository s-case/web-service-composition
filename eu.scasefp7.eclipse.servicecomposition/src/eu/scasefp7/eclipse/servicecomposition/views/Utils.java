package eu.scasefp7.eclipse.servicecomposition.views;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;

public class Utils {

	
	public static void cleanOutputs(Tree outputsComposite, edu.uci.ics.jung.graph.Graph<OwlService, Connector> jungGraph) {
		try {
			for (int j = 0; j < outputsComposite.getChildren().length; j++) {
				if (outputsComposite.getChildren()[j] instanceof Text) {
					Text text = (Text) outputsComposite.getChildren()[j];
					text.setText("");
				}
			}
		} catch (Exception ex) {

		}
		for (OwlService service : jungGraph.getVertices()) {
			if (service.getOperation() != null) {
				for (Argument output : service.getOperation().getOutputs()) {
					int size = output.getElements().size();
					cleanElements(output, size, outputsComposite);

					int size2 = output.getSubtypes().size();
					cleanElements(output, size2, outputsComposite);

				}
			}
		}

	}

	private static void cleanElements(Argument output, int size, Object obj) {

		
		if (obj instanceof Tree) {
			for (int j = 0; j < ((Tree) obj).getItemCount(); j++) {
				if (((Tree) obj).getItem(j) instanceof TreeItem) {
					TreeItem item = (TreeItem) ((Tree) obj).getItem(j);
					if (item.getText().split("\\[")[0].trim().equals(output.getName().toString())) {
						if (output.isArray()) {
							disposeElements(item, item.getItemCount());

						} else {
							if (!output.getSubtypes().isEmpty()) {
								disposeElements(item, item.getItemCount());
							}
						}
					}
				}
			}
		} else {
			for (int j = 0; j < ((TreeItem) obj).getItemCount(); j++) {
				if (((TreeItem) obj).getItem(j) instanceof TreeItem) {
					TreeItem item = (TreeItem) ((TreeItem) obj).getItem(j);
					if (item.getText().split("\\[")[0].trim().equals(output.getName().toString())) {
						if (output.isArray()) {
							disposeElements(item, item.getItemCount());
						} else {
							if (!output.getSubtypes().isEmpty()) {
								disposeElements(item, item.getItemCount());
							}
						}
					}
				}
			}
		}

	}

	private static void disposeElements(TreeItem item, int size) {
		for (int k = 0; k < size; k++) {
			TreeItem item2 = (TreeItem) item.getItem(0);
			item2.dispose();
		}
	}

	
	public static void addArguments(Argument arg, ArrayList<Argument> possibleArguments) {

		for (Argument sub : arg.getSubtypes()) {

			addArguments(sub, possibleArguments);
		}
		possibleArguments.add(arg);

	}
	
}
