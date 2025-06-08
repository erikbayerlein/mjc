package org.mjc.canon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mjc.exceptions.CanonException;
import org.mjc.irtree.*;
import org.mjc.temp.Label;

import java.util.HashMap;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class TraceSchedule {

	public StmList stms;
	BasicBlocks bBlocks;
	@Builder.Default
	HashMap<Label, StmList> table = new HashMap<>();

	public TraceSchedule(BasicBlocks b) {
		table = new HashMap<>();
		bBlocks = b;
		for (var l = b.blocks; l != null; l = l.tail) {
			table.put(((LABEL) l.head.head).label, l.head);
		}
		stms = getNext();
		table = null;
	}

	StmList getLast(StmList block) {
		StmList l = block;
		while (l.tail.tail != null) {
			l = l.tail;
		}
		return l;
	}

	void trace(StmList l) {
		for (;;) {
			var lab = (LABEL) l.head;
			table.remove(lab.label);
			StmList last = getLast(l);
			Stm s = last.tail.head;
			switch (s) {
				case JUMP j -> {
					StmList target = table.get(j.targets.head);
					if (j.targets.tail == null && target != null) {
						last.tail = target;
						l = target;
					} else {
						last.tail.tail = getNext();
						return;
					}
				}
				case CJUMP j -> {
					StmList t = table.get(j.iftrue);
					StmList f = table.get(j.iffalse);
					if (f != null) {
						last.tail.tail = f;
						l = f;
					} else if (t != null) {
						last.tail.head = CJUMP.builder()
							.relop(CJUMP.notRel(j.relop))
							.left(j.left)
							.right(j.right)
							.iftrue(j.iftrue)
							.iffalse(j.iffalse)
							.build();
						last.tail.tail = t;
						l = t;
					} else {
						var ff = new Label();
						last.tail.head = CJUMP.builder()
							.relop(j.relop)
							.left(j.left)
							.right(j.right)
							.iftrue(j.iftrue)
							.iffalse(ff)
							.build();
						last.tail.tail = StmList.builder()
							.head(new LABEL(ff))
							.tail(
								StmList.builder()
									.head(new JUMP(j.iffalse))
									.tail(getNext())
									.build()
							)
							.build();
						return;
					}
				}
				default -> throw new CanonException("Bad basic block in TraceSchedule");
			}
		}
	}

	StmList getNext() {
		if (bBlocks.blocks == null) {
			return new StmList(new LABEL(bBlocks.done), null);
		} else {
			StmList s = bBlocks.blocks.head;
			var lab = (LABEL) s.head;
			if (table.get(lab.label) != null) {
				trace(s);
				return s;
			} else {
				bBlocks.blocks = bBlocks.blocks.tail;
				return getNext();
			}
		}
	}
}
