package eu.scasefp7.eclipse.servicecomposition.ui;

import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;



public class TreeDialog extends Dialog {
	private ArrayList<Operation> operations = new ArrayList<Operation>();
	private Display disp;
	private Operation operation;

	public TreeDialog(Shell parentShell) {
		super(parentShell);
	}

	public void setOperations(ArrayList<Operation> operations) {
		this.operations = operations;
	}

	public void setDisp(Display disp) {
		this.disp = disp;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public Operation getOperation() {
		return this.operation;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		// Button button = new Button(container, SWT.PUSH);
		// button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
		// false,
		// false));
		// button.setText("Press me");
		// button.addSelectionListener(new SelectionAdapter() {
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		// System.out.println("Pressed");
		// }
		// });

		TreeViewer tree = new TreeViewer(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		TreeViewerColumn column1 = new TreeViewerColumn(tree, SWT.LEFT);
		column1.getColumn().setText("Attribute");
		column1.getColumn().setWidth(200);
		column1.getColumn().setResizable(true);
		TreeViewerColumn column2 = new TreeViewerColumn(tree, SWT.LEFT);
		column2.getColumn().setText("Value");
		column2.getColumn().setWidth(200);
		column2.getColumn().setResizable(true);

		Vector<OperationNode> nodes = new Vector<OperationNode>();

		for (Operation operation : operations) {
			OperationNode n = new OperationNode(operation.getName().toString(), null, operation, null, "");
			OperationNode subn1 = new OperationNode("Inputs", n, operation, null, "");
			OperationNode subn2 = new OperationNode("Outputs", n, operation, null, "");
			OperationNode subn3 = new OperationNode("URL", n, operation, null, operation.getDomain().getURI());
			column2.setLabelProvider(createTreeColumnLabelProvider());
			for (Argument input : operation.getInputs()) {
				OperationNode inputn = new OperationNode("", subn1, operation, input, input.getName().toString());
				column2.setLabelProvider(createTreeColumnLabelProvider());
			}
			for (Argument output : operation.getOutputs()) {
				OperationNode outputn = new OperationNode("", subn2, operation, output,
						output.getName().toString());
				column2.setLabelProvider(createTreeColumnLabelProvider());
			}

			nodes.add(n);

		}

		tree.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setContentProvider(new TreeContentProvider());
		column1.setLabelProvider(new TreeLabelProvider());
		tree.setInput(nodes);

		// the viewer field is an already configured TreeViewer
		Tree tree2 = (Tree) tree.getControl();

		Listener listener = new Listener() {

			@Override
			public void handleEvent(Event event) {
				TreeItem treeItem = (TreeItem) event.item;
				final TreeColumn[] treeColumns = treeItem.getParent().getColumns();
				disp.asyncExec(new Runnable() {

					@Override
					public void run() {
						for (TreeColumn treeColumn : treeColumns)
							treeColumn.pack();
					}
				});
			}
		};

		tree2.addListener(SWT.Expand, listener);

		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					if (selection.getFirstElement() != null) {
						if (((OperationNode) selection.getFirstElement()).getParent() == null) {
							setOperation(((OperationNode) selection.getFirstElement()).getOperation());
						} else {
							setOperation(((OperationNode) selection.getFirstElement()).getOperation());
							tree.setSelection(StructuredSelection.EMPTY);
						}
					}

				}
			}
		});

		// tree.expandAll();

		return container;
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	public void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("S-CASE Operations");
	}

	public void setDialogLocation() {
		Rectangle monitorArea = getShell().getDisplay().getPrimaryMonitor().getBounds();
		Rectangle shellArea = getShell().getBounds();
		int x = monitorArea.x + (monitorArea.width - shellArea.width) / 2;
		int y = monitorArea.y + (monitorArea.height - shellArea.height) / 2;
		getShell().setLocation(x, y);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}
	
	
	
	
	
	class OperationNode {
		private String name;
		private String value;
		private Operation operation;
		private Argument argument;
		private Vector<OperationNode> subCategories;
		private OperationNode parent;

		public OperationNode(String name, OperationNode parent, Operation service, Argument argument, String value) {
			if (service != null) {
				this.name = name;
			} else if (argument != null) {
				this.name = name + " [" + argument.getType() + "]:";
			}
			this.parent = parent;
			this.operation = service;
			this.argument = argument;
			this.value = value;

			if (parent != null)
				parent.addSubCategory(this);
		}

		public Vector<OperationNode> getSubCategories() {
			return subCategories;
		}

		private void addSubCategory(OperationNode subcategory) {
			if (subCategories == null)
				subCategories = new Vector<OperationNode>();
			if (!subCategories.contains(subcategory))
				subCategories.add(subcategory);
		}

		public String getName() {
			return name;
		}

		public Argument getArgument() {
			return argument;
		}

		public Operation getOperation() {
			return operation;
		}

		public String getValue() {
			return value;
		}

		public OperationNode getParent() {
			return parent;
		}
	}

	
	
	class TreeLabelProvider extends ColumnLabelProvider implements ILabelProvider {
		public String getText(Object element) {
			return ((OperationNode) element).getName();
		}

		public Image getImage(Object arg0) {
			return null;
		}

		public void addListener(ILabelProviderListener arg0) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object arg0, String arg1) {
			return false;
		}

		public void removeListener(ILabelProviderListener arg0) {
		}
	}

	class TreeContentProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			Vector<OperationNode> subcats = ((OperationNode) parentElement).getSubCategories();
			return subcats == null ? new Object[0] : subcats.toArray();
		}

		public Object getParent(Object element) {
			return ((OperationNode) element).getParent();
		}

		public boolean hasChildren(Object element) {
			return ((OperationNode) element).getSubCategories() != null;
		}

		public Object[] getElements(Object inputElement) {
			if (inputElement != null && inputElement instanceof Vector) {
				return ((Vector<OperationNode>) inputElement).toArray();
			}
			return new Object[0];
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private ColumnLabelProvider createTreeColumnLabelProvider() {
		return new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				return ((OperationNode) element).getValue();
			}

		};
	}

}
