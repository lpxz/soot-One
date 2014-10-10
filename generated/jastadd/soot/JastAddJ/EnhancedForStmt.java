package soot.JastAddJ;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.io.File;
import java.util.*;
import beaver.*;
import java.util.ArrayList;
import java.util.zip.*;
import java.io.*;
import java.io.FileNotFoundException;
import java.util.Collection;
import soot.*;
import soot.util.*;
import soot.jimple.*;
import soot.coffi.ClassFile;
import soot.coffi.method_info;
import soot.coffi.CONSTANT_Utf8_info;
import soot.tagkit.SourceFileTag;
import soot.coffi.CoffiMethodSource;

/**
 * @ast node
 * @declaredat EnhancedFor.ast:1
 */
public class EnhancedForStmt extends BranchTargetStmt implements Cloneable, VariableScope {
  /**
   * @apilevel low-level
   */
  public void flushCache() {
    super.flushCache();
    targetOf_ContinueStmt_values = null;
    targetOf_BreakStmt_values = null;
    canCompleteNormally_computed = false;
    isDAafter_Variable_values = null;
    isDUafter_Variable_values = null;
    cond_label_computed = false;
    cond_label_value = null;
    update_label_computed = false;
    update_label_value = null;
    end_label_computed = false;
    end_label_value = null;
    extraLocalIndex_computed = false;
  }
  /**
   * @apilevel internal
   */
  public void flushCollectionCache() {
    super.flushCollectionCache();
  }
  /**
   * @apilevel internal
   */
  @SuppressWarnings({"unchecked", "cast"})
  public EnhancedForStmt clone() throws CloneNotSupportedException {
    EnhancedForStmt node = (EnhancedForStmt)super.clone();
    node.targetOf_ContinueStmt_values = null;
    node.targetOf_BreakStmt_values = null;
    node.canCompleteNormally_computed = false;
    node.isDAafter_Variable_values = null;
    node.isDUafter_Variable_values = null;
    node.cond_label_computed = false;
    node.cond_label_value = null;
    node.update_label_computed = false;
    node.update_label_value = null;
    node.end_label_computed = false;
    node.end_label_value = null;
    node.extraLocalIndex_computed = false;
    node.in$Circle(false);
    node.is$Final(false);
    return node;
  }
  /**
   * @apilevel internal
   */
  @SuppressWarnings({"unchecked", "cast"})
  public EnhancedForStmt copy() {
      try {
        EnhancedForStmt node = (EnhancedForStmt)clone();
        if(children != null) node.children = (ASTNode[])children.clone();
        return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
  }
  /**
   * @apilevel low-level
   */
  @SuppressWarnings({"unchecked", "cast"})
  public EnhancedForStmt fullCopy() {
    EnhancedForStmt res = (EnhancedForStmt)copy();
    for(int i = 0; i < getNumChildNoTransform(); i++) {
      ASTNode node = getChildNoTransform(i);
      if(node != null) node = node.fullCopy();
      res.setChild(node, i);
    }
    return res;
    }
  /**
   * @ast method 
   * @aspect EnhancedFor
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/EnhancedFor.jrag:15
   */
  public void typeCheck() {
		if (!getExpr().type().isArrayDecl() && !getExpr().type().isIterable()) {
			error("type " + getExpr().type().name() + 
			      " of expression in foreach is neither array type nor java.lang.Iterable");
		}	
    else if(getExpr().type().isArrayDecl() && !getExpr().type().componentType().assignConversionTo(getVariableDeclaration().type(), null))
      error("parameter of type " + getVariableDeclaration().type().typeName() + " can not be assigned an element of type " + getExpr().type().componentType().typeName()); 
    else if(getExpr().type().isIterable() && !getExpr().type().isUnknown()) {
      MethodDecl iterator = (MethodDecl)getExpr().type().memberMethods("iterator").iterator().next();
      MethodDecl next = (MethodDecl)iterator.type().memberMethods("next").iterator().next();
      TypeDecl componentType = next.type();
      if(!componentType.assignConversionTo(getVariableDeclaration().type(), null))
        error("parameter of type " + getVariableDeclaration().type().typeName() + " can not be assigned an element of type " + componentType.typeName()); 
    }
	}
  /**
   * @ast method 
   * @aspect EnhancedFor
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/EnhancedFor.jrag:58
   */
  public void toString(StringBuffer s) {
    s.append(indent());
    s.append("for (");
    getVariableDeclaration().getModifiers().toString(s);
    getVariableDeclaration().getTypeAccess().toString(s);
    s.append(" " + getVariableDeclaration().name());
    s.append(" : ");
    getExpr().toString(s);
    s.append(") ");
    getStmt().toString(s);
  }
  /**
   * @ast method 
   * @aspect EnhancedForToBytecode
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddExtensions/Jimple1.5Backend/EnhancedForCodegen.jrag:24
   */
  public void jimplify2(Body b) {
    if(getExpr().type().isArrayDecl()) {
      soot.Local array = asLocal(b, getExpr().eval(b));
      soot.Local index = asLocal(b, soot.jimple.IntConstant.v(0));
      soot.Local parameter = b.newLocal(getVariableDeclaration().name(), getVariableDeclaration().type().getSootType());
      getVariableDeclaration().local = parameter;
	  b.setLine(this);
      b.addLabel(cond_label());
      b.add(
        b.newIfStmt(
          b.newGeExpr(
            asImmediate(b, index),
            asImmediate(b, b.newLengthExpr(asImmediate(b, array), this)),
            this
          ),
          end_label(),
          this
        )
      );
      b.add(
        b.newAssignStmt(
          parameter,
          asRValue(b,
            getExpr().type().elementType().emitCastTo(b,
              asLocal(b,
                b.newArrayRef(
                  array,
                  index,
                  this
                )
              ),
              getVariableDeclaration().type(),
              this
            )
          ),
          this
        )
      );
      getStmt().jimplify2(b);
      b.addLabel(update_label());
      b.add(
        b.newAssignStmt(
          index,
          b.newAddExpr(
            index,
            soot.jimple.IntConstant.v(1),
            this
          ),
          this
        )
      );
      b.add(b.newGotoStmt(cond_label(), this));
      b.addLabel(end_label());
    }
    else {
      soot.Local iterator = asLocal(b,
        b.newInterfaceInvokeExpr(
          asLocal(b, getExpr().eval(b)),
          iteratorMethod().sootRef(),
          new ArrayList(),
          this
        )
      );
      soot.Local parameter = b.newLocal(getVariableDeclaration().name(), getVariableDeclaration().type().getSootType());
      getVariableDeclaration().local = parameter;
      b.addLabel(cond_label());
      b.add(
        b.newIfStmt(
          b.newEqExpr(
            asImmediate(b, 
              b.newInterfaceInvokeExpr(
                iterator,
                hasNextMethod().sootRef(),
                new ArrayList(),
                this
              )
            ),
            BooleanType.emitConstant(false),
            this
          ),
          end_label(),
          this
        )
      );
      b.add(
        b.newAssignStmt(
          parameter,
          nextMethod().type().emitCastTo(b,
            b.newInterfaceInvokeExpr(
              iterator,
              nextMethod().sootRef(),
              new ArrayList(),
              this
            ),
            getVariableDeclaration().type(),
            this
          ),
          this
        )
      );
      getStmt().jimplify2(b);
      b.addLabel(update_label());
      b.add(b.newGotoStmt(cond_label(), this));
      b.addLabel(end_label());



      /*
      getExpr().createBCode(gen);
      iteratorMethod().emitInvokeMethod(gen, lookupType("java.lang", "Iterable"));
      gen.emitStoreReference(extraLocalIndex());
      gen.addLabel(cond_label());
      gen.emitLoadReference(extraLocalIndex());
      hasNextMethod().emitInvokeMethod(gen, lookupType("java.util", "Iterator"));
      gen.emitCompare(Bytecode.IFEQ, end_label());
      gen.emitLoadReference(extraLocalIndex());
      nextMethod().emitInvokeMethod(gen, lookupType("java.util", "Iterator"));
      gen.emitCheckCast(getVariableDeclaration().type());
      gen.emitStoreReference(getVariableDeclaration().localNum());
      getStmt().createBCode(gen);
      gen.addLabel(update_label());	
      gen.emitGoto(cond_label());
      gen.addLabel(end_label());
      */
    }
  }
  /**
   * @ast method 
   * @aspect EnhancedForToBytecode
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddExtensions/Jimple1.5Backend/EnhancedForCodegen.jrag:151
   */
  private MethodDecl iteratorMethod() {
    TypeDecl typeDecl = lookupType("java.lang", "Iterable");
		for (Iterator iter = typeDecl.memberMethods("iterator").iterator(); iter.hasNext();) {
			MethodDecl m = (MethodDecl)iter.next();
			if (m.getNumParameter() == 0) {
				return m;
      }
    }
    throw new Error("Could not find java.lang.Iterable.iterator()");
  }
  /**
   * @ast method 
   * @aspect EnhancedForToBytecode
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddExtensions/Jimple1.5Backend/EnhancedForCodegen.jrag:161
   */
  private MethodDecl hasNextMethod() {
    TypeDecl typeDecl = lookupType("java.util", "Iterator");
		for (Iterator iter = typeDecl.memberMethods("hasNext").iterator(); iter.hasNext();) {
			MethodDecl m = (MethodDecl)iter.next();
			if (m.getNumParameter() == 0) {
				return m;
      }
    }
    throw new Error("Could not find java.util.Collection.hasNext()");
  }
  /**
   * @ast method 
   * @aspect EnhancedForToBytecode
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddExtensions/Jimple1.5Backend/EnhancedForCodegen.jrag:171
   */
  private MethodDecl nextMethod() {
    TypeDecl typeDecl = lookupType("java.util", "Iterator");
		for (Iterator iter = typeDecl.memberMethods("next").iterator(); iter.hasNext();) {
			MethodDecl m = (MethodDecl)iter.next();
			if (m.getNumParameter() == 0) {
				return m;
      }
    }
    throw new Error("Could not find java.util.Collection.next()");
  }
  /**
   * @ast method 
   * @declaredat EnhancedFor.ast:1
   */
  public EnhancedForStmt() {
    super();


  }
  /**
   * @ast method 
   * @declaredat EnhancedFor.ast:7
   */
  public EnhancedForStmt(VariableDeclaration p0, Expr p1, Stmt p2) {
    setChild(p0, 0);
    setChild(p1, 1);
    setChild(p2, 2);
  }
  /**
   * @apilevel low-level
   * @ast method 
   * @declaredat EnhancedFor.ast:15
   */
  protected int numChildren() {
    return 3;
  }
  /**
   * @apilevel internal
   * @ast method 
   * @declaredat EnhancedFor.ast:21
   */
  public boolean mayHaveRewrite() {
    return false;
  }
  /**
   * Setter for VariableDeclaration
   * @apilevel high-level
   * @ast method 
   * @declaredat EnhancedFor.ast:5
   */
  public void setVariableDeclaration(VariableDeclaration node) {
    setChild(node, 0);
  }
  /**
   * Getter for VariableDeclaration
   * @apilevel high-level
   * @ast method 
   * @declaredat EnhancedFor.ast:12
   */
  public VariableDeclaration getVariableDeclaration() {
    return (VariableDeclaration)getChild(0);
  }
  /**
   * @apilevel low-level
   * @ast method 
   * @declaredat EnhancedFor.ast:18
   */
  public VariableDeclaration getVariableDeclarationNoTransform() {
    return (VariableDeclaration)getChildNoTransform(0);
  }
  /**
   * Setter for Expr
   * @apilevel high-level
   * @ast method 
   * @declaredat EnhancedFor.ast:5
   */
  public void setExpr(Expr node) {
    setChild(node, 1);
  }
  /**
   * Getter for Expr
   * @apilevel high-level
   * @ast method 
   * @declaredat EnhancedFor.ast:12
   */
  public Expr getExpr() {
    return (Expr)getChild(1);
  }
  /**
   * @apilevel low-level
   * @ast method 
   * @declaredat EnhancedFor.ast:18
   */
  public Expr getExprNoTransform() {
    return (Expr)getChildNoTransform(1);
  }
  /**
   * Setter for Stmt
   * @apilevel high-level
   * @ast method 
   * @declaredat EnhancedFor.ast:5
   */
  public void setStmt(Stmt node) {
    setChild(node, 2);
  }
  /**
   * Getter for Stmt
   * @apilevel high-level
   * @ast method 
   * @declaredat EnhancedFor.ast:12
   */
  public Stmt getStmt() {
    return (Stmt)getChild(2);
  }
  /**
   * @apilevel low-level
   * @ast method 
   * @declaredat EnhancedFor.ast:18
   */
  public Stmt getStmtNoTransform() {
    return (Stmt)getChildNoTransform(2);
  }
  /**
   * @attribute syn
   * @aspect EnhancedFor
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/EnhancedFor.jrag:50
   */
  @SuppressWarnings({"unchecked", "cast"})
  public SimpleSet localLookupVariable(String name) {
      ASTNode$State state = state();
    SimpleSet localLookupVariable_String_value = localLookupVariable_compute(name);
    return localLookupVariable_String_value;
  }
  /**
   * @apilevel internal
   */
  private SimpleSet localLookupVariable_compute(String name) {
		if(getVariableDeclaration().name().equals(name)) {
      return SimpleSet.emptySet.add(getVariableDeclaration());
    }
 	  return lookupVariable(name);
	}
  protected java.util.Map targetOf_ContinueStmt_values;
  /**
   * @attribute syn
   * @aspect EnhancedFor
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/EnhancedFor.jrag:75
   */
  @SuppressWarnings({"unchecked", "cast"})
  public boolean targetOf(ContinueStmt stmt) {
    Object _parameters = stmt;
    if(targetOf_ContinueStmt_values == null) targetOf_ContinueStmt_values = new java.util.HashMap(4);
    if(targetOf_ContinueStmt_values.containsKey(_parameters)) {
      return ((Boolean)targetOf_ContinueStmt_values.get(_parameters)).booleanValue();
    }
      ASTNode$State state = state();
  int num = state.boundariesCrossed;
  boolean isFinal = this.is$Final();
    boolean targetOf_ContinueStmt_value = targetOf_compute(stmt);
if(isFinal && num == state().boundariesCrossed) targetOf_ContinueStmt_values.put(_parameters, Boolean.valueOf(targetOf_ContinueStmt_value));
    return targetOf_ContinueStmt_value;
  }
  /**
   * @apilevel internal
   */
  private boolean targetOf_compute(ContinueStmt stmt) {  return !stmt.hasLabel();  }
  protected java.util.Map targetOf_BreakStmt_values;
  /**
   * @attribute syn
   * @aspect EnhancedFor
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/EnhancedFor.jrag:76
   */
  @SuppressWarnings({"unchecked", "cast"})
  public boolean targetOf(BreakStmt stmt) {
    Object _parameters = stmt;
    if(targetOf_BreakStmt_values == null) targetOf_BreakStmt_values = new java.util.HashMap(4);
    if(targetOf_BreakStmt_values.containsKey(_parameters)) {
      return ((Boolean)targetOf_BreakStmt_values.get(_parameters)).booleanValue();
    }
      ASTNode$State state = state();
  int num = state.boundariesCrossed;
  boolean isFinal = this.is$Final();
    boolean targetOf_BreakStmt_value = targetOf_compute(stmt);
if(isFinal && num == state().boundariesCrossed) targetOf_BreakStmt_values.put(_parameters, Boolean.valueOf(targetOf_BreakStmt_value));
    return targetOf_BreakStmt_value;
  }
  /**
   * @apilevel internal
   */
  private boolean targetOf_compute(BreakStmt stmt) {  return !stmt.hasLabel();  }
  /**
   * @apilevel internal
   */
  protected boolean canCompleteNormally_computed = false;
  /**
   * @apilevel internal
   */
  protected boolean canCompleteNormally_value;
  /**
   * @attribute syn
   * @aspect EnhancedFor
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/EnhancedFor.jrag:79
   */
  @SuppressWarnings({"unchecked", "cast"})
  public boolean canCompleteNormally() {
    if(canCompleteNormally_computed) {
      return canCompleteNormally_value;
    }
      ASTNode$State state = state();
  int num = state.boundariesCrossed;
  boolean isFinal = this.is$Final();
    canCompleteNormally_value = canCompleteNormally_compute();
if(isFinal && num == state().boundariesCrossed) canCompleteNormally_computed = true;
    return canCompleteNormally_value;
  }
  /**
   * @apilevel internal
   */
  private boolean canCompleteNormally_compute() {  return reachable();  }
  protected java.util.Map isDAafter_Variable_values;
  /**
   * @attribute syn
   * @aspect EnhancedFor
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/EnhancedFor.jrag:83
   */
  @SuppressWarnings({"unchecked", "cast"})
  public boolean isDAafter(Variable v) {
    Object _parameters = v;
    if(isDAafter_Variable_values == null) isDAafter_Variable_values = new java.util.HashMap(4);
    if(isDAafter_Variable_values.containsKey(_parameters)) {
      return ((Boolean)isDAafter_Variable_values.get(_parameters)).booleanValue();
    }
      ASTNode$State state = state();
  int num = state.boundariesCrossed;
  boolean isFinal = this.is$Final();
    boolean isDAafter_Variable_value = isDAafter_compute(v);
if(isFinal && num == state().boundariesCrossed) isDAafter_Variable_values.put(_parameters, Boolean.valueOf(isDAafter_Variable_value));
    return isDAafter_Variable_value;
  }
  /**
   * @apilevel internal
   */
  private boolean isDAafter_compute(Variable v) {
    if(!getExpr().isDAafter(v))
      return false;
    /*
    for(Iterator iter = targetBreaks().iterator(); iter.hasNext(); ) {
      BreakStmt stmt = (BreakStmt)iter.next();
      if(!stmt.isDAafterReachedFinallyBlocks(v))
        return false;
    }
    */
    return true;
  }
  protected java.util.Map isDUafter_Variable_values;
  /**
   * @attribute syn
   * @aspect EnhancedFor
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/EnhancedFor.jrag:99
   */
  @SuppressWarnings({"unchecked", "cast"})
  public boolean isDUafter(Variable v) {
    Object _parameters = v;
    if(isDUafter_Variable_values == null) isDUafter_Variable_values = new java.util.HashMap(4);
    if(isDUafter_Variable_values.containsKey(_parameters)) {
      return ((Boolean)isDUafter_Variable_values.get(_parameters)).booleanValue();
    }
      ASTNode$State state = state();
  int num = state.boundariesCrossed;
  boolean isFinal = this.is$Final();
    boolean isDUafter_Variable_value = isDUafter_compute(v);
if(isFinal && num == state().boundariesCrossed) isDUafter_Variable_values.put(_parameters, Boolean.valueOf(isDUafter_Variable_value));
    return isDUafter_Variable_value;
  }
  /**
   * @apilevel internal
   */
  private boolean isDUafter_compute(Variable v) {
    if(!getExpr().isDUafter(v))
      return false;
    for(Iterator iter = targetBreaks().iterator(); iter.hasNext(); ) {
      BreakStmt stmt = (BreakStmt)iter.next();
      if(!stmt.isDUafterReachedFinallyBlocks(v))
        return false;
    }
    return true;
  }
  /**
   * @attribute syn
   * @aspect EnhancedFor
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/EnhancedFor.jrag:114
   */
  @SuppressWarnings({"unchecked", "cast"})
  public boolean continueLabel() {
      ASTNode$State state = state();
    boolean continueLabel_value = continueLabel_compute();
    return continueLabel_value;
  }
  /**
   * @apilevel internal
   */
  private boolean continueLabel_compute() {  return true;  }
  /**
   * @apilevel internal
   */
  protected boolean cond_label_computed = false;
  /**
   * @apilevel internal
   */
  protected soot.jimple.Stmt cond_label_value;
  /**
   * @attribute syn
   * @aspect EnhancedForToBytecode
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddExtensions/Jimple1.5Backend/EnhancedForCodegen.jrag:12
   */
  @SuppressWarnings({"unchecked", "cast"})
  public soot.jimple.Stmt cond_label() {
    if(cond_label_computed) {
      return cond_label_value;
    }
      ASTNode$State state = state();
  int num = state.boundariesCrossed;
  boolean isFinal = this.is$Final();
    cond_label_value = cond_label_compute();
if(isFinal && num == state().boundariesCrossed) cond_label_computed = true;
    return cond_label_value;
  }
  /**
   * @apilevel internal
   */
  private soot.jimple.Stmt cond_label_compute() {  return newLabel();  }
  /**
   * @apilevel internal
   */
  protected boolean update_label_computed = false;
  /**
   * @apilevel internal
   */
  protected soot.jimple.Stmt update_label_value;
  /**
   * @attribute syn
   * @aspect EnhancedForToBytecode
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddExtensions/Jimple1.5Backend/EnhancedForCodegen.jrag:13
   */
  @SuppressWarnings({"unchecked", "cast"})
  public soot.jimple.Stmt update_label() {
    if(update_label_computed) {
      return update_label_value;
    }
      ASTNode$State state = state();
  int num = state.boundariesCrossed;
  boolean isFinal = this.is$Final();
    update_label_value = update_label_compute();
if(isFinal && num == state().boundariesCrossed) update_label_computed = true;
    return update_label_value;
  }
  /**
   * @apilevel internal
   */
  private soot.jimple.Stmt update_label_compute() {  return newLabel();  }
  /**
   * @apilevel internal
   */
  protected boolean end_label_computed = false;
  /**
   * @apilevel internal
   */
  protected soot.jimple.Stmt end_label_value;
  /**
   * @attribute syn
   * @aspect EnhancedForToBytecode
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddExtensions/Jimple1.5Backend/EnhancedForCodegen.jrag:14
   */
  @SuppressWarnings({"unchecked", "cast"})
  public soot.jimple.Stmt end_label() {
    if(end_label_computed) {
      return end_label_value;
    }
      ASTNode$State state = state();
  int num = state.boundariesCrossed;
  boolean isFinal = this.is$Final();
    end_label_value = end_label_compute();
if(isFinal && num == state().boundariesCrossed) end_label_computed = true;
    return end_label_value;
  }
  /**
   * @apilevel internal
   */
  private soot.jimple.Stmt end_label_compute() {  return newLabel();  }
  /**
   * @apilevel internal
   */
  protected boolean extraLocalIndex_computed = false;
  /**
   * @apilevel internal
   */
  protected int extraLocalIndex_value;
  /**
   * @attribute syn
   * @aspect EnhancedForToBytecode
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddExtensions/Jimple1.5Backend/EnhancedForCodegen.jrag:16
   */
  @SuppressWarnings({"unchecked", "cast"})
  public int extraLocalIndex() {
    if(extraLocalIndex_computed) {
      return extraLocalIndex_value;
    }
      ASTNode$State state = state();
  int num = state.boundariesCrossed;
  boolean isFinal = this.is$Final();
    extraLocalIndex_value = extraLocalIndex_compute();
if(isFinal && num == state().boundariesCrossed) extraLocalIndex_computed = true;
    return extraLocalIndex_value;
  }
  /**
   * @apilevel internal
   */
  private int extraLocalIndex_compute() {  return localNum();  }
  /**
   * @attribute syn
   * @aspect EnhancedForToBytecode
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddExtensions/Jimple1.5Backend/EnhancedForCodegen.jrag:21
   */
  @SuppressWarnings({"unchecked", "cast"})
  public soot.jimple.Stmt break_label() {
      ASTNode$State state = state();
    soot.jimple.Stmt break_label_value = break_label_compute();
    return break_label_value;
  }
  /**
   * @apilevel internal
   */
  private soot.jimple.Stmt break_label_compute() {  return end_label();  }
  /**
   * @attribute syn
   * @aspect EnhancedForToBytecode
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddExtensions/Jimple1.5Backend/EnhancedForCodegen.jrag:22
   */
  @SuppressWarnings({"unchecked", "cast"})
  public soot.jimple.Stmt continue_label() {
      ASTNode$State state = state();
    soot.jimple.Stmt continue_label_value = continue_label_compute();
    return continue_label_value;
  }
  /**
   * @apilevel internal
   */
  private soot.jimple.Stmt continue_label_compute() {  return update_label();  }
  /**
   * @attribute inh
   * @aspect EnhancedFor
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/EnhancedFor.jrag:38
   */
  @SuppressWarnings({"unchecked", "cast"})
  public SimpleSet lookupVariable(String name) {
      ASTNode$State state = state();
    SimpleSet lookupVariable_String_value = getParent().Define_SimpleSet_lookupVariable(this, null, name);
    return lookupVariable_String_value;
  }
  /**
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/EnhancedFor.jrag:41
   * @apilevel internal
   */
  public SimpleSet Define_SimpleSet_lookupVariable(ASTNode caller, ASTNode child, String name) {
    if(caller == getStmtNoTransform()) {
      return localLookupVariable(name);
    }
    if(caller == getExprNoTransform()) {
      return localLookupVariable(name);
    }
    if(caller == getVariableDeclarationNoTransform()) {
      return localLookupVariable(name);
    }
    return getParent().Define_SimpleSet_lookupVariable(this, caller, name);
  }
  /**
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/EnhancedFor.jrag:43
   * @apilevel internal
   */
  public NameType Define_NameType_nameType(ASTNode caller, ASTNode child) {
    if(caller == getVariableDeclarationNoTransform()) {
      return NameType.TYPE_NAME;
    }
    return getParent().Define_NameType_nameType(this, caller);
  }
  /**
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/EnhancedFor.jrag:48
   * @apilevel internal
   */
  public VariableScope Define_VariableScope_outerScope(ASTNode caller, ASTNode child) {
    if(caller == getStmtNoTransform()) {
      return this;
    }
    if(caller == getExprNoTransform()) {
      return this;
    }
    if(caller == getVariableDeclarationNoTransform()) {
      return this;
    }
    return getParent().Define_VariableScope_outerScope(this, caller);
  }
  /**
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/EnhancedFor.jrag:71
   * @apilevel internal
   */
  public boolean Define_boolean_isMethodParameter(ASTNode caller, ASTNode child) {
    if(caller == getVariableDeclarationNoTransform()) {
      return false;
    }
    return getParent().Define_boolean_isMethodParameter(this, caller);
  }
  /**
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/EnhancedFor.jrag:72
   * @apilevel internal
   */
  public boolean Define_boolean_isConstructorParameter(ASTNode caller, ASTNode child) {
    if(caller == getVariableDeclarationNoTransform()) {
      return false;
    }
    return getParent().Define_boolean_isConstructorParameter(this, caller);
  }
  /**
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/EnhancedFor.jrag:73
   * @apilevel internal
   */
  public boolean Define_boolean_isExceptionHandlerParameter(ASTNode caller, ASTNode child) {
    if(caller == getVariableDeclarationNoTransform()) {
      return false;
    }
    return getParent().Define_boolean_isExceptionHandlerParameter(this, caller);
  }
  /**
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/EnhancedFor.jrag:80
   * @apilevel internal
   */
  public boolean Define_boolean_reachable(ASTNode caller, ASTNode child) {
    if(caller == getStmtNoTransform()) {
      return reachable();
    }
    return getParent().Define_boolean_reachable(this, caller);
  }
  /**
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/EnhancedFor.jrag:97
   * @apilevel internal
   */
  public boolean Define_boolean_isDAbefore(ASTNode caller, ASTNode child, Variable v) {
    if(caller == getStmtNoTransform()) {
      return getExpr().isDAafter(v);
    }
    if(caller == getExprNoTransform()) {
      return v == getVariableDeclaration() || isDAbefore(v);
    }
    return getParent().Define_boolean_isDAbefore(this, caller, v);
  }
  /**
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/EnhancedFor.jrag:111
   * @apilevel internal
   */
  public boolean Define_boolean_isDUbefore(ASTNode caller, ASTNode child, Variable v) {
    if(caller == getStmtNoTransform()) {
      return getExpr().isDUafter(v);
    }
    if(caller == getExprNoTransform()) {
      return v != getVariableDeclaration() && isDUbefore(v);
    }
    return getParent().Define_boolean_isDUbefore(this, caller, v);
  }
  /**
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/EnhancedFor.jrag:113
   * @apilevel internal
   */
  public boolean Define_boolean_insideLoop(ASTNode caller, ASTNode child) {
    if(caller == getStmtNoTransform()) {
      return true;
    }
    return getParent().Define_boolean_insideLoop(this, caller);
  }
  /**
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddExtensions/Jimple1.5Backend/EnhancedForCodegen.jrag:18
   * @apilevel internal
   */
  public int Define_int_localNum(ASTNode caller, ASTNode child) {
    if(caller == getStmtNoTransform()) {
      return getVariableDeclaration().localNum() + getVariableDeclaration().type().size();
    }
    if(caller == getVariableDeclarationNoTransform()) {
      return localNum() + (getExpr().type().isArrayDecl() ? 2 : 1);
    }
    return getParent().Define_int_localNum(this, caller);
  }
  /**
   * @apilevel internal
   */
  public ASTNode rewriteTo() {
    return super.rewriteTo();
  }
}
