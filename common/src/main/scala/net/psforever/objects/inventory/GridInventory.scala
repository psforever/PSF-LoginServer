// Copyright (c) 2017 PSForever
package net.psforever.objects.inventory

import java.util.concurrent.atomic.AtomicInteger

import net.psforever.objects.equipment.{Equipment, EquipmentSlot}
import net.psforever.types.PlanetSideGUID

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

/**
  * An inventory are used to stow `Equipment` when it does not exist visually in the game world.<br>
  * <br>
  * Visually, an inventory is understood as a rectangular region divided into cellular units.
  * The `Equipment` that is placed into the inventory can also be represented as smaller rectangles, also composed of cells.
  * The same number of cells of the item must overlap with the same number of cells of the inventory.
  * No two items may have cells that overlap.
  * This "grid" maintains a spatial distinction between items when they get stowed.<br>
  * <br>
  * It is not necessary to actually have a structural representation of the "grid."
  * Adhering to such a data structure does speed up the actions upon the inventory and its contents in certain cases (where noted).
  * The `HashMap` of items is used for quick object lookup.
  * Use of the `HashMap` only is hitherto referred as "using the inventory as a `List`."
  * The `Array` of spatial GUIDs is used for quick collision lookup.
  * Use of the `Array` only is hitherto referred as "using the inventory as a grid."
  */
class GridInventory extends Container {
  private var width : Int = 1
  private var height : Int = 1
  private var offset : Int = 0 //the effective index of the first cell in the inventory where offset >= 0

  /* key - an integer (not especially meaningful beyond being unique); value - the card that represents the stowed item */
  private val items : mutable.HashMap[Int, InventoryItem] = mutable.HashMap[Int, InventoryItem]()
  private val entryIndex : AtomicInteger = new AtomicInteger(0)
  private var grid : Array[Int] = Array.fill[Int](1)(-1)

  def Items : List[InventoryItem] = items.values.toList

  def Width : Int = width

  def Height : Int = height

  def Offset : Int = offset

  /**
    * Change the grid index offset value.
    * @param fset the new offset value
    * @return the current offset value
    * @throws IndexOutOfBoundsException if the index is negative
    */
  def Offset_=(fset : Int) : Int = {
    if(fset < 0) {
      throw new IndexOutOfBoundsException(s"can not set index offset to negative number - $fset")
    }
    offset = fset
    Offset
  }

  def Size : Int = items.size

  /**
    * Capacity is a measure how many squares in the grid inventory are unused (value of -1).
    * It does not guarantee the cells are distributed in any configuration conductive to item stowing.
    * @return the number of free cells
    */
  def Capacity : Int = {
    TotalCapacity - items.values.foldLeft(0)((cnt, item) => cnt + (item.obj.Tile.Width * item.obj.Tile.Height))
  }

  /**
    * The total number of cells in this inventory.
    * @return the width multiplied by the height (`grid.length`, which is the same thing)
    */
  def TotalCapacity : Int = grid.length

  /**
    * The index of the last cell in this inventory.
    * @return same as `Offset` plus the total number of cells in this inventory minus 1
    */
  def LastIndex : Int = Offset + TotalCapacity - 1

  override def Find(guid : PlanetSideGUID) : Option[Int] = {
    items.values.find({ case InventoryItem(obj, _) => obj.HasGUID && obj.GUID == guid}) match {
      case Some(InventoryItem(_, index)) =>
        Some(index)
      case None =>
        None
    }
  }

  /**
    * Get whatever is stowed in the inventory at the given index.
    * @param slot the cell index
    * @return an `EquipmentSlot` that contains whatever `Equipment` was stored in `slot`
    */
  override def Slot(slot : Int) : EquipmentSlot = {
    val actualSlot = slot - offset
    if(actualSlot < 0 || actualSlot > grid.length) {
      throw new IndexOutOfBoundsException(s"requested indices not in bounds of grid inventory - $actualSlot")
    }
    else {
      new InventoryEquipmentSlot(slot, this)
    }
  }

  /**
    * Test whether a given piece of `Equipment` would collide with any stowed content in the inventory.<br>
    * <br>
    * A "collision" is considered a situation where the stowed placards of two items would overlap in some way.
    * The gridkeeps track of the location of items by storing the primitive of their GUID in one or more cells.
    * Two primitives can not be stored in the same cell.
    * If placing two items into the same inventory leads to a situation where two primitive values might be in the same cell,
    * that is a collision.
    * @param start the cell index to test this `Equipment` for insertion
    * @param item the `Equipment` to be tested
    * @return a `List` of GUID values for all existing contents that this item would overlap if inserted
    */
  def CheckCollisions(start : Int, item : Equipment) : Try[List[Int]] = {
    val tile : InventoryTile = item.Tile
    CheckCollisions(start, tile.Width, tile.Height)
  }

  /**
    * Test whether a given piece of `Equipment` would collide with any stowed content in the inventory.
    * @param start the cell index to test this `Equipment` for insertion
    * @param w the width of the `Equipment` to be tested
    * @param h the height of the `Equipment` to be tested
    * @return a `List` of GUID values for all existing contents that this item would overlap if inserted
    */
  def CheckCollisions(start : Int, w : Int, h : Int) : Try[List[Int]] = {
    if(items.isEmpty) {
      Success(List.empty[Int])
    }
    else {
      CheckCollisionsVar(start, w, h) match {
        case Success(list) =>
          Success(list.map({ f => f.obj.GUID.guid }))
        case Failure(ex) =>
          Failure(ex)
      }
    }
  }

  /**
    * Test whether a given piece of `Equipment` would collide with any stowed content in the inventory.<br>
    * <br>
    * If there are fewer items stored in the inventory than there are cells required to represent the testing item,
    * test the collision by iterating through the list of items.
    * If there are more items, check that each cell that would be used for the testing items tile does not collide.
    * The "testing item" in this case has already been transformed into its tile dimensions.
    * @param start the cell index to test this `Equipment` for insertion
    * @param w the width of the `Equipment` to be tested
    * @param h the height of the `Equipment` to be tested
    * @return a `List` of existing items that an item of this scale would overlap if inserted
    */
  def CheckCollisionsVar(start : Int, w : Int, h : Int) : Try[List[InventoryItem]] = {
    if(items.size < w * h) {
      CheckCollisionsAsList(start, w, h)
    }
    else {
      CheckCollisionsAsGrid(start, w, h)
    }
  }

  /**
    * Test whether a given piece of `Equipment` would collide with any stowed content in the inventory.<br>
    * <br>
    * Iterate over all stowed items and check each one whether or not it overlaps with the given region.
    * This is a "using the inventory as a `List`" method.
    * @param start the cell index to test this `Equipment` for insertion
    * @param w the width of the `Equipment` to be tested
    * @param h the height of the `Equipment` to be tested
    * @return a `List` of existing items that an item of this scale would overlap if inserted
    * @throws IndexOutOfBoundsException if the region extends outside of the grid boundaries
    */
  def CheckCollisionsAsList(start : Int, w : Int, h : Int) : Try[List[InventoryItem]] = {
    val actualSlot = start - offset
    val startx : Int = actualSlot % width
    val starty : Int = actualSlot / width
    val startw : Int = startx + w - 1
    val starth : Int = starty + h - 1
    if(actualSlot < 0 || actualSlot >= grid.length || startw >= width || starth >= height) {
      val bounds : String = if(startx < 0) { "left" } else if(startw >= width) { "right" } else { "bottom" }
      Failure(new IndexOutOfBoundsException(s"requested region escapes the $bounds edge of the grid inventory - $startx + $w, $starty + $h"))
    }
    else {
      val collisions : mutable.Set[InventoryItem] = mutable.Set[InventoryItem]()
      items.values.foreach({ item : InventoryItem =>
        val actualItemStart : Int = item.start - offset
        val itemx : Int = actualItemStart % width
        val itemy : Int = actualItemStart / width
        val tile = item.obj.Tile
        val clipsOnX : Boolean = if(itemx < startx) { itemx + tile.Width > startx } else { itemx <= startw }
        val clipsOnY : Boolean = if(itemy < starty) { itemy + tile.Height > starty } else { itemy <= starth }
        if(clipsOnX && clipsOnY) {
          collisions += item
        }
      })
      Success(collisions.toList)
    }
  }

  /**
    * Test whether a given piece of `Equipment` would collide with any stowed content in the inventory.<br>
    * <br>
    * Iterate over all cells that would be occupied by a new value and check each one whether or not that cell has an existing value.
    * This is a "using the inventory as a grid" method.
    * @param start the cell index to test this `Equipment` for insertion
    * @param w the width of the `Equipment` to be tested
    * @param h the height of the `Equipment` to be tested
    * @return a `List` of existing items that an item of this scale would overlap if inserted
    * @throws IndexOutOfBoundsException if the region extends outside of the grid boundaries
    */
  def CheckCollisionsAsGrid(start : Int, w : Int, h : Int) : Try[List[InventoryItem]] = {
    val actualSlot = start - offset
    if(actualSlot < 0 || actualSlot >= grid.length || (actualSlot % width) + w > width || (actualSlot / width) + h > height) {
      val startx : Int = actualSlot % width
      val starty : Int = actualSlot / width
      val startw : Int = startx + w - 1
      val bounds : String = if(startx < 0) { "left" } else if(startw >= width) { "right" } else { "bottom" }
      Failure(new IndexOutOfBoundsException(s"requested region escapes the $bounds edge of the grid inventory - $startx + $w, $starty + $h"))
    }
    else {
      val collisions : mutable.Set[InventoryItem] = mutable.Set[InventoryItem]()
      var curr = actualSlot
      val fixedItems = items.toMap
      val fixedGrid = grid.toList
      try {
        for(_ <- 0 until h) {
          for(col <- 0 until w) {
            val itemIndex = fixedGrid(curr + col)
            if(itemIndex > -1) {
              collisions += fixedItems(itemIndex)
            }
          }
          curr += width
        }
        Success(collisions.toList)
      }
      catch {
        case e : NoSuchElementException =>
          Failure(InventoryDisarrayException(s"inventory contained old item data", e))
        case e : Exception =>
          Failure(e)
      }
    }
  }

  /**
    * Find a blank space in the current inventory where a `tile` of given dimensions can be cleanly inserted.
    * Brute-force method.
    * @param tile the dimensions of the blank space
    * @return the grid index of the upper left corner where equipment to which the `tile` belongs should be placed
    */
  override def Fit(tile : InventoryTile) : Option[Int] = {
    val tWidth = tile.Width
    val tHeight = tile.Height
    val gridIter = (0 until (grid.length - (tHeight - 1) * width))
      .filter(cell => grid(cell) == -1 && (width - cell%width >= tWidth))
      .iterator
    recursiveFitTest(gridIter, tWidth, tHeight)
  }

  /**
    * Find a blank space in the current inventory where a `tile` of given dimensions can be cleanly inserted.
    * @param cells an iterator of all accepted indices in the `grid`
    * @param tWidth the width of the blank space
    * @param tHeight the height of the blank space
    * @return the grid index of the upper left corner where equipment to which the `tile` belongs should be placed
    */
  @tailrec private def recursiveFitTest(cells : Iterator[Int], tWidth : Int, tHeight : Int) : Option[Int] = {
    if(!cells.hasNext) {
      None
    }
    else {
      val index = cells.next + offset
      CheckCollisionsAsGrid(index, tWidth, tHeight) match {
        case Success(Nil) =>
          Some(index)
        case Success(_) =>
          recursiveFitTest(cells, tWidth, tHeight)
        case Failure(ex) =>
          throw ex
      }
    }
  }

  /**
    * Define a region of inventory grid cells and set them to a given value.
    * @param start the initial inventory index
    * @param w the width of the region
    * @param h the height of the region
    * @param value the value to set all the cells in the defined region;
    *              defaults to -1 (which is "nothing")
    */
  def SetCells(start : Int, w : Int, h : Int, value : Int = -1) : Unit = {
    SetCellsOffset(math.max(start - offset, 0), w, h, value)
  }

  /**
    * Define a region of inventory grid cells and set them to a given value.
    * @param start the initial inventory index, without the inventory offset (required)
    * @param w the width of the region
    * @param h the height of the region
    * @param value the value to set all the cells in the defined region;
    *              defaults to -1 (which is "nothing")
    * @throws IndexOutOfBoundsException if the region extends outside of the grid boundaries
    */
  def SetCellsOffset(start : Int, w : Int, h : Int, value : Int = -1) : Unit = {
    if(start < 0 || start > grid.length || (start % width) + w - 1 > width || (start / width) + h- 1 > height) {
      val startx : Int = start % width
      val starty : Int = start / width
      val startw : Int = startx + w - 1
      val bounds : String = if(startx < 0) { "left" } else if(startw >= width) { "right" } else { "bottom" }
      throw new IndexOutOfBoundsException(s"requested region escapes the $bounds of the grid inventory - $startx + $w, $starty + $h")
    }
    else {
      var curr = start
      for(_ <- 0 until h) {
        for(col <- 0 until w) {
          grid(curr + col) = value
        }
        curr += width
      }
    }
  }

  def Insert(start : Int, obj : Equipment) : Boolean = {
    val key : Int = entryIndex.getAndIncrement()
    items.get(key) match {
      case None => //no redundant insertions or other collisions
        Insertion_CheckCollisions(start, obj, key)
      case _ =>
        false
    }
  }

  def Insertion_CheckCollisions(start : Int, obj : Equipment, key : Int) : Boolean = {
    CheckCollisions(start, obj) match {
      case Success(Nil) =>
        InsertQuickly(start, obj, key)
      case _ =>
        false
    }
  }

  def InsertQuickly(start : Int, obj : Equipment) : Boolean = InsertQuickly(start, obj, entryIndex.getAndIncrement())

  private def InsertQuickly(start : Int, obj : Equipment, key : Int) : Boolean = {
    val card = InventoryItem(obj, start)
    items += key -> card
    val tile = obj.Tile
    SetCells(start, tile.Width, tile.Height, key)
    true
  }

  def +=(kv : (Int, Equipment)) : Boolean = Insert(kv._1, kv._2)

  def Remove(index : Int) : Boolean = {
    val key = grid(index - Offset)
    items.remove(key) match {
      case Some(item) =>
        val tile = item.obj.Tile
        SetCells(item.start, tile.Width, tile.Height)
        true
      case None =>
        false
    }
  }

  def -=(index : Int) : Boolean = Remove(index)

  def Remove(guid : PlanetSideGUID) : Boolean = {
    recursiveFindIdentifiedObject(items.keys.iterator, guid) match {
      case Some(index) =>
        val item = items.remove(index).get
        val tile = item.obj.Tile
        SetCells(item.start, tile.Width, tile.Height)
        true
      case None =>
        false
    }
  }

  def -=(guid : PlanetSideGUID) : Boolean = Remove(guid)

  @tailrec private def recursiveFindIdentifiedObject(iter : Iterator[Int], guid : PlanetSideGUID) : Option[Int] = {
    if(!iter.hasNext) {
      None
    }
    else {
      val index = iter.next
      if(items(index).obj.GUID == guid) {
        Some(index)
      }
      else {
        recursiveFindIdentifiedObject(iter, guid)
      }
    }
  }

  /**
    * Does this inventory contain an object with the given GUID?
    * @param guid the GUID
    * @return the discovered object, or `None`
    */
  def hasItem(guid : PlanetSideGUID) : Option[Equipment] = {
    recursiveFindIdentifiedObject(items.keys.iterator, guid) match {
      case Some(index) =>
        Some(items(index).obj)
      case None =>
        None
    }
  }

  /**
    * Clear the inventory by removing all of its items.
    * @return a `List` of the previous items in the inventory as their `InventoryItemData` tiles
    */
  def Clear() : List[InventoryItem] = {
    val list = items.values.toList
    items.clear
    SetCellsOffset(0, width, height)
    list
  }

  /**
    * Change the size of the inventory, without regard for its current contents.
    * This method replaces mutators for `Width` and `Height`.
    * @param w the new width
    * @param h the new height
    * @throws IllegalArgumentException if the new size to be set is zero or less
    */
  def Resize(w : Int, h : Int) : Unit = {
    if(w < 1 || h < 1) {
      throw new IllegalArgumentException("area of inventory space must not be < 1")
    }
    width = w
    height = h
    grid = Array.fill[Int](w * h)(-1)
  }

  def VisibleSlots : Set[Int] = Set.empty[Int]

  def Inventory = this
}

object GridInventory {
  /**
    * Overloaded constructor.
    * @return a `GridInventory` object
    */
  def apply() : GridInventory = {
    new GridInventory()
  }

  /**
    * Overloaded constructor for initializing an inventory of specific dimensions.
    * @param width the horizontal size of the inventory
    * @param height the vertical size of the inventory
    * @return a `GridInventory` object
    */
  def apply(width : Int, height : Int) : GridInventory = {
    val obj = new GridInventory()
    obj.Resize(width, height)
    obj
  }

  /**
    * Overloaded constructor for initializing an inventory of specific dimensions and index offset.
    * @param width the horizontal size of the inventory
    * @param height the vertical size of the inventory
    * @param offset the effective index of the first cell in the inventory
    * @return a `GridInventory` object
    */
  def apply(width : Int, height : Int, offset : Int) : GridInventory = {
    val obj = new GridInventory()
    obj.Resize(width, height)
    obj.Offset = offset
    obj
  }

  /**
    * Accepting items that may or may not have previously been in an inventory,
    * determine if there is a tight-fit arrangement for the items in the given inventory.
    * Note that arrangement for future insertion.
    * @param list a `List` of items to be potentially re-inserted
    * @param predicate a condition to sort the previous `List` of elements
    * @param inv the inventory in which they would be re-inserted in the future
    * @return two `List`s of `Equipment`;
    *         the first `List` is composed of `InventoryItemData`s that will be reinserted at the new `start` index;
    *         the second list is composed of `Equipment` that will not be put back into the inventory
    */
  def recoverInventory(list : List[InventoryItem], inv : GridInventory, predicate : (InventoryItem, InventoryItem) => Boolean = StandardScaleSort) : (List[InventoryItem], List[Equipment]) = {
    sortKnapsack(
      list.sortWith(predicate),
      inv.width,
      inv.height
    )
    val (elements, out) = list.partition(p => p.start > -1)
    elements.foreach(item => item.start += inv.Offset)
    (elements, out.map(item => item.obj))
  }

  /**
    * The default predicate used by the knapsack sort algorithm.
    */
  final val StandardScaleSort : (InventoryItem, InventoryItem) => Boolean =
    (a, b) => {
      val aTile = a.obj.Tile
      val bTile = b.obj.Tile
      if(aTile.Width == bTile.Width) {
        aTile.Height > bTile.Height
      }
      else {
        aTile.Width > bTile.Width
      }
    }

  /**
    * Start calculating the "optimal" fit for a `List` of items in an inventory of given size.<br>
    * <br>
    * The initial dimensions always fit a space of 0,0 to `width`, `height`.
    * As locations for elements are discovered, the `start` index for that `List` element is changed in-place.
    * If an element can not be re-inserted according to the algorithm, the `start` index is set to an invalid -1.
    * @param list a `List` of items to be potentially re-inserted
    * @param width the horizontal length of the inventory
    * @param height the vertical length of the inventory
    */
  private def sortKnapsack(list : List[InventoryItem], width : Int, height : Int) : Unit = {
    val root = new KnapsackNode(0, 0, width, height)
    list.foreach(item => {
      findKnapsackSpace(root, item.obj.Tile.Width, item.obj.Tile.Height) match {
        case Some(node) =>
          splitKnapsackSpace(node, item.obj.Tile.Width, item.obj.Tile.Height)
          item.start = node.y * width + node.x
        case _ => ;
          item.start = -1
      }
    })
  }

  /**
    * A binary tree node suitable for executing a hasty solution to the knapsack problem.<br>
    * <br>
    * All children are flush with their parent node and with each other.
    * Horizontal space for the `down` child is emphasized over vertical space for the `right` child.
    * By dividing and reducing a defined space like this, it can be tightly packed with a given number of elements.<br>
    * <br>
    * Due to the nature of the knapsack problem and the naivette of the algorithm, small holes in the solution are bound to crop-up.
    * @param x the x-coordinate, upper left corner
    * @param y the y-coordinate, upper left corner
    * @param width the width
    * @param height the height
    */
  private class KnapsackNode(var x : Int, var y : Int, var width : Int, var height : Int) {
    private var used : Boolean = false
    var down : Option[KnapsackNode] = None
    var right : Option[KnapsackNode] = None

    def Used : Boolean = used

    /**
      * Initialize the `down` and `right` children of this node.
      */
    def Split() : Unit = {
      used = true
      down = Some(new KnapsackNode(0,0,0,0))
      right = Some(new KnapsackNode(0,0,0,0))
    }

    /**
      * Change the dimensions of the node.<br>
      * <br>
      * Use: `{node}(nx, ny, nw, nh)`
      * @param nx the new x-coordinate, upper left corner
      * @param ny the new y-coordinate, upper left corner
      * @param nw the new width
      * @param nh the new height
      */
    def apply(nx : Int, ny : Int, nw : Int, nh : Int) : Unit = {
      x = nx
      y = ny
      width = nw
      height = nh
    }
  }

  /**
    * Search this node and its children for a space that can be occupied by an element of given dimensions.
    * @param node the current node
    * @param width width of the element
    * @param height height of the element
    * @return the selected node
    */
  private def findKnapsackSpace(node : KnapsackNode, width : Int, height : Int) : Option[KnapsackNode] = {
    if(node.Used) {
      findKnapsackSpace(node.right.get, width, height).orElse(findKnapsackSpace(node.down.get, width, height))
    }
    else if(width <= node.width && height <= node.height) {
      Some(node)
    }
    else {
      None
    }
  }

  /**
    * Populate the `down` and `right` nodes for the knapsack sort.<br>
    * <br>
    * This function carves node into three pieces.
    * The third piece is the unspoken space occupied by the element of given dimensions.
    * Specifically: `node.x`, `node.y` to `width`, `height`.
    * @param node the current node
    * @param width width of the element
    * @param height height of the element
    */
  private def splitKnapsackSpace(node : KnapsackNode, width : Int, height : Int) : Unit = {
    node.Split()
    node.down.get(node.x, node.y + height, node.width, node.height - height)
    node.right.get(node.x + width, node.y, node.width - width, height)
  }
}
