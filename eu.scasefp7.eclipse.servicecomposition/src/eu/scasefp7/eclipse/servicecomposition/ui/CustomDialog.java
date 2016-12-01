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
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.ui.TreeDialog.ReplaceInformationNode;

public class CustomDialog extends Dialog {
	private ArrayList<Operation> operations = new ArrayList<Operation>();
	private ArrayList<Operation> PWoperations = new ArrayList<Operation>();
	private ArrayList<Operation> Mashapeoperations = new ArrayList<Operation>();
	private Display disp;
	private Operation operation;
	private String title;
	private String tab = "S-CASE Operations";

	public CustomDialog(Shell parent, String title) {
		super(parent);
		this.title = title;
	}

	public void setOperations(ArrayList<Operation> operations) {
		this.operations = operations;
	}

	public void setPWOperations(ArrayList<Operation> operations) {
		this.PWoperations = operations;
	}
	
	public void setMashapeOperations(ArrayList<Operation> operations) {
		this.Mashapeoperations = operations;
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
		TabFolder folder = new TabFolder(container, SWT.TOP);
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText("S-CASE Operations");
		item.setControl(createTree(folder, "s-case"));

		TabItem item2 = new TabItem(folder, SWT.NONE);
		item2.setText("PW Operations");
		item2.setControl(createTree(folder, "PW"));
		
		TabItem item3 = new TabItem(folder, SWT.NONE);
		item3.setText("Mashape Operations");
		item3.setControl(createTree(folder, "Mashape"));
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
		shell.setSize(400, 800);
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

	protected Control createTree(TabFolder folder, String mode) {

		TreeViewer tree = new TreeViewer(folder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		TreeViewerColumn column1 = new TreeViewerColumn(tree, SWT.LEFT );
		column1.getColumn().setText("Attribute");
		column1.getColumn().setWidth(300);
		column1.getColumn().setResizable(true);
		// TreeViewerColumn column2 = new TreeViewerColumn(tree, SWT.LEFT);
		// column2.getColumn().setText("Value");
		// column2.getColumn().setWidth(200);
		// column2.getColumn().setResizable(true);

		tree.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
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
				}

				return order;
			};
		});

		column1.setLabelProvider(new TreeLabelProvider());

		Vector<OperationNode> nodes = new Vector<OperationNode>();
		if (mode.equals("s-case")) {
			for (Operation operation : operations) {
				OperationNode n = createOperationNode(operation);
				nodes.add(n);

			}
		} else if (mode.equals("PW")) {
			for (Operation operation : PWoperations) {
				OperationNode n = createOperationNode(operation);
				nodes.add(n);

			}
		}
		else if (mode.equals("Mashape")) {
			for (Operation operation : Mashapeoperations) {
				OperationNode n = createOperationNode(operation);
				nodes.add(n);

			}
		}
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
						if (selection.getFirstElement() instanceof OperationNode) {
							if (((OperationNode) selection.getFirstElement()).getParent() == null) {
								setOperation(((OperationNode) selection.getFirstElement()).getOperation());
							} else {
								setOperation(((OperationNode) selection.getFirstElement()).getOperation());
								tree.setSelection(StructuredSelection.EMPTY);
							}
						}
					}

				}
			}
		});
		
		 folder.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
		        System.out.println(folder.getSelection()[0].getText() + " selected");
		        setTabNumber(folder.getSelection()[0].getText());
		      }
		    });

		return tree.getControl();
	}
	
	private void setTabNumber(String tab){
		this.tab = tab;
	}
	
	public String getTabNumber(){
		return tab ;
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

			return (OperationNode) parent;

		}

		public void setParent(Object parent) {
			this.parent = parent;
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
