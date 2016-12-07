package eu.scasefp7.eclipse.servicecomposition.ui;

import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
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
import org.eclipse.jface.viewers.ViewerComparator;
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
import eu.scasefp7.eclipse.servicecomposition.transformer.Transformer.ReplaceInformation;
import eu.scasefp7.eclipse.servicecomposition.ui.CustomDialog.OperationNode;

public class TreeDialog extends Dialog {
	private ArrayList<Operation> operations = new ArrayList<Operation>();
	private Display disp;
	private Operation operation;
	private ArrayList<ReplaceInformation> replaceInformations = new ArrayList<ReplaceInformation>();
	private ReplaceInformation replaceInformation;
	private String mode = "operations";
	private String title;

	public TreeDialog(Shell parentShell, String title) {
		super(parentShell);
		this.title = title;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public void setOperations(ArrayList<Operation> operations) {
		this.operations = operations;
	}

	public void setReplaceInformations(ArrayList<ReplaceInformation> replaceInformations) {
		this.replaceInformations = replaceInformations;
	}

	public void setDisp(Display disp) {
		this.disp = disp;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public void setReplaceInformation(ReplaceInformation replaceInformation) {
		this.replaceInformation = replaceInformation;
	}

	public Operation getOperation() {
		return this.operation;
	}

	public ReplaceInformation getReplaceInformation() {
		return this.replaceInformation;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);

		TreeViewer tree = new TreeViewer(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

		TreeViewerColumn column1 = new TreeViewerColumn(tree, SWT.LEFT);
		column1.getColumn().setText("Attribute");
		column1.getColumn().setWidth(300);
		column1.getColumn().setResizable(true);
		// TreeViewerColumn column2 = new TreeViewerColumn(tree, SWT.LEFT);
		// column2.getColumn().setText("Value");
		// column2.getColumn().setWidth(200);
		// column2.getColumn().setResizable(true);

		tree.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setContentProvider(new TreeContentProvider());

		// sort alphabetically
		tree.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				int order = 0;
				if (e1 instanceof OperationNode) {
					OperationNode t1 = (OperationNode) e1;
					OperationNode t2 = (OperationNode) e2;
					order = ((t1.getName()).compareTo(t2.getName()));
				} else {
					ReplaceInformationNode t1 = (ReplaceInformationNode) e1;
					ReplaceInformationNode t2 = (ReplaceInformationNode) e2;
					order = (((Double)(t1.getReplaceInformation().getWeight())).compareTo(t2.getReplaceInformation().getWeight()));
				}
				
				return order;
			};
		});

		column1.setLabelProvider(new TreeLabelProvider());

		if (mode.equals("operations")) {
			Vector<OperationNode> nodes = new Vector<OperationNode>();

			for (Operation operation : operations) {

				OperationNode n = createOperationNode(operation);
				// column2.setLabelProvider(createTreeColumnLabelProvider());
				nodes.add(n);

			}
			tree.setInput(nodes);
		} else if (mode.equals("replaceInfo")) {
			Vector<ReplaceInformationNode> nodes = new Vector<ReplaceInformationNode>();
			int i = 1;
			for (ReplaceInformation replaceInformation : replaceInformations) {
				OperationNode n = createOperationNode(replaceInformation.getOperationToReplace());
				ReplaceInformationNode rn = new ReplaceInformationNode("Replacement option" + i, null, n,
						replaceInformation);
				ReplaceInformationNode subrn = new ReplaceInformationNode(
						replaceInformation.getOperationToReplace().getName().toString(), rn, n, replaceInformation);
				// column2.setLabelProvider(new TreeLabelProvider());
				nodes.add(rn);
				i++;
			}

			tree.setInput(nodes);
			tree.expandToLevel(2);
		}

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
						if (selection.getFirstElement() instanceof OperationNode) {
							if (((OperationNode) selection.getFirstElement()).getParent() == null) {
								setOperation(((OperationNode) selection.getFirstElement()).getOperation());
							} else {
								if (((OperationNode) selection.getFirstElement())
										.getParent() instanceof ReplaceInformationNode) {
									setReplaceInformation(
											(((ReplaceInformationNode) ((OperationNode) selection.getFirstElement())
													.getParent()).getReplaceInformation()));
								}
								setOperation(((OperationNode) selection.getFirstElement()).getOperation());
								tree.setSelection(StructuredSelection.EMPTY);
							}
						} else if (selection.getFirstElement() instanceof ReplaceInformationNode) {
							if (((ReplaceInformationNode) selection.getFirstElement()).getParent() == null) {
								setReplaceInformation(
										((ReplaceInformationNode) selection.getFirstElement()).getReplaceInformation());
							} else {
								setReplaceInformation(
										((ReplaceInformationNode) selection.getFirstElement()).getReplaceInformation());
								tree.setSelection(StructuredSelection.EMPTY);
							}
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
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null) {
			shell.setText(title);
		}
		//shell.setMinimumSize(200, 200);
		shell.setSize(500, 300);
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

	public OperationNode createOperationNode(Operation operation) {
		OperationNode n = new OperationNode(operation.getName().toString(), null, operation, null, "");
		OperationNode subn1 = new OperationNode("Inputs", n, operation, null, "");
		OperationNode subn2 = new OperationNode("Outputs", n, operation, null, "");
		OperationNode subn3 = new OperationNode("URL", n, operation, null, operation.getDomain().getURI());
		OperationNode subn4 = new OperationNode("Domain", n, operation, null, "");

		for (Argument input : operation.getInputs()) {
			OperationNode inputn = new OperationNode(input.getName().toString(), subn1, operation, input,
					input.getName().toString());
		}
		for (Argument output : operation.getOutputs()) {
			OperationNode outputn = new OperationNode(output.getName().toString(), subn2, operation, output,
					output.getName().toString());
		}
		OperationNode urln = new OperationNode(operation.getDomain().getURI(), subn3, operation, null,
				operation.getDomain().getURI());
		for (String domainName : operation.getDomain().getDomains()){
			OperationNode domainn = new OperationNode(domainName, subn4, operation, null,
					domainName);
		}
		return (n);
	}

	class OperationNode {
		private String name;
		private String value;
		private Operation operation;
		private Argument argument;
		private Vector<OperationNode> subCategories;
		private Object parent;

		public OperationNode(String name, Object parent, Operation service, Argument argument, String value) {
			if (service != null) {
				this.name = name;
			}
			if (argument != null) {
				this.name = name + " [" + argument.getType() + "]";
			}
			this.parent = parent;
			this.operation = service;
			this.argument = argument;
			this.value = value;

			if ((OperationNode) parent != null) {
				if (parent instanceof OperationNode)
					((OperationNode) parent).addSubCategory(this);
			}
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

		public Object getParent() {
			if (parent instanceof OperationNode) {
				return (OperationNode) parent;
			} else {
				return (ReplaceInformationNode) parent;
			}
		}

		public void setParent(Object parent) {
			this.parent = parent;
		}
	}

	class ReplaceInformationNode {
		private String name;
		private OperationNode operation;
		private ReplaceInformation replaceInformation;
		private Vector<OperationNode> subCategories;
		private ReplaceInformationNode parent;

		public ReplaceInformationNode(String name, ReplaceInformationNode parent, OperationNode service,
				ReplaceInformation replaceInformation) {
			if (service != null) {
				this.name = name;
			}
			this.parent = parent;
			this.operation = service;
			this.replaceInformation = replaceInformation;

			if (parent != null) {
				service.setParent((ReplaceInformationNode) parent);
				parent.addSubCategory(service);
			}
		}

		public Vector<OperationNode> getSubCategories() {
			return subCategories;
		}

		public ReplaceInformation getReplaceInformation() {
			return replaceInformation;
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

		public OperationNode getOperation() {
			return operation;
		}

		public ReplaceInformationNode getParent() {
			return parent;
		}
	}

	class TreeLabelProvider extends ColumnLabelProvider implements ILabelProvider {
		public String getText(Object element) {
			if (element instanceof OperationNode) {
				return ((OperationNode) element).getName();
			} else {
				return ((ReplaceInformationNode) element).getName();
			}
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
			Vector<OperationNode> subcats = null;
			if (parentElement instanceof OperationNode) {
				subcats = ((OperationNode) parentElement).getSubCategories();
			} else if (parentElement instanceof ReplaceInformationNode) {
				subcats = ((ReplaceInformationNode) parentElement).getSubCategories();

			}
			return subcats == null ? new Object[0] : subcats.toArray();
		}

		public Object getParent(Object element) {
			if (element instanceof OperationNode) {
				return ((OperationNode) element).getParent();
			} else {
				return ((ReplaceInformationNode) element).getParent();
			}
		}

		public boolean hasChildren(Object element) {
			if (element instanceof OperationNode) {
				return ((OperationNode) element).getSubCategories() != null;
			} else {
				return ((ReplaceInformationNode) element).getSubCategories() != null;
			}
		}

		public Object[] getElements(Object inputElement) {
			if (inputElement != null && inputElement instanceof Vector) {
				if (inputElement instanceof OperationNode) {
					return ((Vector<OperationNode>) inputElement).toArray();
				} else {
					return ((Vector<ReplaceInformationNode>) inputElement).toArray();
				}
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
