/*
The MIT License (MIT)

Copyright (c) 2015 Pierre Lindenbaum

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.


*/
package com.github.lindenb.jvarkit.util.picard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;

public abstract class Prediction
	{
	public static interface Parser<T extends Prediction>
		{
		public List<T> parse(VariantContext ctx);
		}
	private static enum SnpEffField {Effect,
		Effect_Impact,
		Functional_Class,
		Codon_Change,
		Amino_Acid_change,
		Amino_Acid_length,
		Gene_Name,
		Gene_BioType,
		Coding,
		Transcript,
		Exon,
		Errors,
		Warnings
		}
	private static class SnpEffParser implements Parser<SnpEff>
		{
		private List<SnpEffField> fields=new ArrayList<Prediction.SnpEffField>(SnpEffField.values().length);
		private Pattern pattern;
		SnpEffParser(VCFInfoHeaderLine header)
			{
			if(header==null) throw new NullPointerException("no header line for SNPEFF.");
			String s=header.getDescription();
			if(s==null) throw new IllegalArgumentException("no description in SNPEFF ?");
			int i=s.indexOf('\'');
			if(i==-1) throw new IllegalArgumentException("cannot find apos in "+s);
			int j=s.lastIndexOf('\'', i+1);
			if(j==-1) throw new IllegalArgumentException("cannot find apos in "+s);
			this.pattern=Pattern.compile(" [ |()]+");
			for(String sub:this.pattern.split(s))
				{
				for(SnpEffField f:SnpEffField.values())
					{
					if(!f.name().equals(sub)) continue;
					this.fields.add(f);
					break;
					}
				}
			}
		@Override
		public List<SnpEff> parse(VariantContext ctx)
			{
			List<SnpEff> list=new ArrayList<SnpEff>();
			Object o=ctx.getAttribute("SNPEFF");
			if(!o.getClass().isArray())
				{
				o=new Object[]{o};
				}
			for(Object o2:(Object[])o)
				{
				SnpEff eff=new SnpEff();
				String tokens[]=this.pattern.split(o2.toString());
				for(int i=0;i< tokens.length && i< this.fields.size();++i)
					{
					if(tokens[i].isEmpty()) continue;
					eff.field2data.put(this.fields.get(i), tokens[i]);
					}
				list.add(eff);
				}
			return list;
			}
		}
	
	public static class SnpEff extends Prediction
		{
		private Map<SnpEffField, String> field2data=new HashMap<Prediction.SnpEffField, String>();
		}
	
	public static class Vep extends Prediction
		{
	
		}
	public static Parser<SnpEff> createSnpEffParser(VCFHeader h)
		{
		return createSnpEffParser(h.getInfoHeaderLine("SNPEFF"));
		}
	/* ##INFO=<ID=EFF,Number=.,Type=String,Description="Predicted effects for this variant.Format: 'Effect ( Effect_Impact | Functional_Class | Codon_Change | Amino_Acid_change| Amino_Acid_length | Gene_Name | Gene_BioType | Coding | Transcript | Exon [ | ERRORS | WARNINGS ] )' \"> */
	public static Parser<SnpEff> createSnpEffParser(VCFInfoHeaderLine header)
		{
		SnpEffParser p=new SnpEffParser(header);
		return p;
		}
	
	}
