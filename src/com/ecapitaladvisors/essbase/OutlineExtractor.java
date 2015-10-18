package com.ecapitaladvisors.essbase;

import java.io.*;

import com.essbase.api.base.*;
import com.essbase.api.datasource.*;
import com.essbase.api.dataquery.*;
import com.essbase.api.metadata.*;
import com.essbase.api.domain.*;
import com.essbase.api.session.IEssbase;
import com.essbase.api.base.EssException;

public class OutlineExtractor {
    
    // Member Objects 
    private static IEssCubeView cv;
    private static IEssbase ess;
    private static IEssDomain dom;
    private static FileOutputStream rpt;
    private static PrintStream ps;
    private static IEssOlapServer svr;
    private static IEssCubeOutline outline;
    private static IEssCube cube;
    private static IEssIterator attributes = null;
    
    // Parameters
    private static String providerURL;
    private static String server;
    private static String app;
    private static String db;
    private static String user;
    private static String pw;
    private static String member;
    private static String dimension;
    private static String outfile = "outline.extract";
    private static String outformat = "PC";
    private static String delimiter = "|";
    private static boolean quietmode = false;
       
    // Public Methods 
    public static void main(String[] args) {
        if(ValidateArgs(args)){
            try{
                File f = new File(outfile);
                if(f.exists())
                    f.delete();
                rpt = new FileOutputStream(f);
                ps = new PrintStream(rpt);
            }
            catch(Exception e){
                System.out.println("\nERROR: Unable to initialize file streams: " + e.getMessage());
                System.out.println("Exiting...\n");
            }
            try{
                ess = IEssbase.Home.create(IEssbase.JAPI_VERSION);
                dom = ess.signOn(user, pw, false, null, providerURL);
                svr = (IEssOlapServer)dom.getOlapServer(server);
                svr.connect();
                cube = svr.getApplication(app).getCube(db);
                outline = cube.openOutline();
            }
            catch(EssException e){
                System.out.println("\nERROR: Unable to connect: " + e.getNativeMessage());
            } 
            try{
                cv = dom.openCubeView("CubeView", server, app, db, false, false, false, true);
                String initialVal = "";
                if(dimension != null){
                    switch(outformat.charAt(0)){
	                    case 80: // 'P'
                        {
                        	IEssMember temp;
	                        if(member == null){
	                            temp = svr.getApplication(app).getCube(db).getDimension(dimension).getDimensionRootMember();
	                            initialVal = "#root";
	                        } 
	                        else{
	                        	temp = outline.findMember(member);
	                        	initialVal = temp.getParentMemberName();
	                        }
	                        String DimType = "Other";
	                        IEssIterator it = outline.getDimensions();
	                        IEssDimension tempDim = null;
	                        for(int i = 0; i < it.getCount(); i++)
	                        	if(((IEssDimension)it.getAt(i)).getName().equalsIgnoreCase(dimension)){
	                        		tempDim = (IEssDimension)it.getAt(i);
	                        		break;
	                        	}
	                        if(tempDim.getCategory() == com.essbase.api.metadata.IEssDimension.EEssDimensionCategory.ACCOUNTS)
	                            DimType = "Account";
	                        ParseMembers_ParentChildFormat(temp, initialVal, DimType, true);
	                        break;
                        }
                        
	                    case 71: // 'G'
	                    {
                        	IEssMember temp;
	                    	if(member == null){
	                            temp = svr.getApplication(app).getCube(db).getDimension(dimension).getDimensionRootMember();
//	                            ParseMembers_GenerationFormat(temp, initialVal);
	                        } 
	                        else{
	                            temp = outline.findMember(member);
	                            IEssMember x = temp;
	                            for(int j = 1; j <= x.getGenerationNumber(); j++){
	                                initialVal = (new StringBuilder(String.valueOf(x.getParentMemberName()))).append(delimiter).append(initialVal).toString();
	                                x = outline.findMember(x.getParentMemberName());
	                            }
	
//	                            ParseMembers_GenerationFormat(temp, initialVal.substring(0, initialVal.lastIndexOf('|')));
	                        }
	                        break;
	                    }
	                    
	                    default:
	                        throw new Exception("An unrecognized file format parameter was passed...");
                    }
                } 
                else{
                    IEssIterator it = svr.getApplication(app).getCube(db).getDimensions();
                    for(int i = 0; i < it.getCount() - 1; i++){
                        dimension = it.getAt(i).toString();
                        switch(outformat.charAt(0)){
	                        case 80: // 'P'
	                        {
	                        	IEssMember temp;
	                            if(member == null){
	                                temp = svr.getApplication(app).getCube(db).getDimension(dimension).getDimensionRootMember();
	                                initialVal = "#root";
	                            }
	                            else{
	                                temp = outline.findMember(member);
		                        	initialVal = temp.getParentMemberName();
	                            }
	                            String DimType = "Other";
	                            IEssIterator it2 = outline.getDimensions();
	                            IEssDimension tempDim = null;
	                            for(int j = 0; j < it2.getCount(); j++)
	                                if(((IEssDimension)it2.getAt(j)).getName().equalsIgnoreCase(dimension))
	                                    tempDim = (IEssDimension)it2.getAt(j);
	
	                            if(tempDim.getCategory() == com.essbase.api.metadata.IEssDimension.EEssDimensionCategory.ACCOUNTS)
	                                DimType = "Account";
	                            ParseMembers_ParentChildFormat(temp, initialVal, DimType, true);
	                            break;
	                        }
	                        
	                        case 71: // 'G'
	                        {
	                        	IEssMember temp;
	                            if(member == null){
	                                temp = svr.getApplication(app).getCube(db).getDimension(dimension).getDimensionRootMember();
//	                                ParseMembers_GenerationFormat(temp, initialVal);
	                                break;
	                            }
	                            temp = outline.findMember(member);
	                            IEssMember x = temp;
	                            for(int j = 1; j <= x.getGenerationNumber(); j++){
	                                initialVal = (new StringBuilder(String.valueOf(x.getParentMemberName()))).append(delimiter).append(initialVal).toString();
	                                x = outline.findMember(x.getParentMemberName());
	                            }
	
//	                            ParseMembers_GenerationFormat(temp, initialVal.substring(0, initialVal.lastIndexOf('|')));
	                            break;
	                        }
	                        
	                        default:
	                            throw new Exception("An unrecognized file format parameter was passed...");
                        }
                    }

                }
                if(!quietmode)
                {
                    System.out.println("\nProcess completed successfully...");
                    System.out.println("Extract created: " + outfile + "\n");
                    System.out.println("Exiting...\n");
                }
            }
            catch(Exception e){
                if(!quietmode)
                    System.out.println("\nERROR: Unable to retrieve member list");
            }
            finally{
                try{
                    svr.disconnect();
                    ess.signOff();
                }
                catch(EssException e){
                    // Do nothing - if ess.signOff() fails, there is no connection
                }
                ps.close();
            }
        }
        else
            PrintUsage();
        return;
    }
    
    // Recursive Methods to read the outline
    private static void ParseMembers_ParentChildFormat(IEssMember node, String parent, String DimType, boolean isRootNode){
 
    	try{
        	String printString = null;
            IEssMember x = outline.findMember(node.getName());
            String UDA = "";
            if(x.getUDAs().length == 1)
                UDA = x.getUDAs()[0];
            else
	            if(x.getUDAs().length > 1){
	                UDA = x.getUDAs()[0];
	                for(int i = 1; i < x.getUDAs().length; i++)
	                    UDA = UDA + "," + x.getUDAs()[i];
	
	            }
            String attribs = "";
            IEssIterator tempIT = null;
            if(x.getAttributeValue() != null){
                tempIT = x.getAssociatedAttributes();
                for(int i = 0; i < tempIT.getCount() - 1; i++)
                    attribs = attribs + tempIT.getAt(i).toString() + delimiter;

                if(tempIT.getCount() > 0)
                    attribs = attribs + tempIT.getAt(tempIT.getCount() - 1).toString();
            }
            String alias = "";
            if(x.getAlias("Default") != null && !x.getName().equalsIgnoreCase(x.getAlias("Default")))
                alias = x.getAlias("Default");
            String parentString = parent;
            String twoPass = "";
            if(!x.getPropertyValueAny("Two pass calc member").toString().trim().equalsIgnoreCase("FALSE"))
                twoPass = "T";
            String consolidation = "";
            if(x.getParentMemberName().equalsIgnoreCase(parent)){
	            if(x.getPropertyValueAny("Share Option").toString().trim().equalsIgnoreCase("Dynamic calc (no store)"))
	                consolidation = "X";
	            else
	            if(x.getPropertyValueAny("Share Option").toString().trim().equalsIgnoreCase("Label only"))
	                consolidation = "O";
	            else
	            if(x.getPropertyValueAny("Share Option").toString().trim().equalsIgnoreCase("Store data"))
	                consolidation = "S";
	            else
	            if(x.getPropertyValueAny("Share Option").toString().trim().equalsIgnoreCase("Dynamic calc and store"))
	                consolidation = "V";
	            else
	            if(x.getPropertyValueAny("Share Option").toString().trim().equalsIgnoreCase("Never share"))
	                consolidation = "N";
	        }
            else{
            	alias="";
            }
            String formula = "";
            if(x.getFormula() != null && !(consolidation.equalsIgnoreCase("")))
                formula = x.getFormula().replaceAll("\"", "\\\\\"").replaceAll("\n", " ").replaceAll("\r", " ");
            String solveOrder = "";
            try{
                solveOrder = Short.toString(x.getSolveOrder());
            }
            catch(EssException essexception){ }
            if(DimType.equalsIgnoreCase("Account"))
            {
                String timeBalance = "";
                if(x.getPropertyValueAny("Time balance option").toString().trim().equalsIgnoreCase("Last"))
                    timeBalance = "L";
                if(x.getPropertyValueAny("Time balance option").toString().trim().equalsIgnoreCase("First"))
                    timeBalance = "F";
                String skipValues = "";
                if(x.getPropertyValueAny("Time balance skip option").toString().trim().equalsIgnoreCase("#Missing and Zero cells"))
                    skipValues = "B";
                if(x.getPropertyValueAny("Time balance skip option").toString().trim().equalsIgnoreCase("#Missing cells"))
                    skipValues = "M";
                if(x.getPropertyValueAny("Time balance skip option").toString().trim().equalsIgnoreCase("Zero cells"))
                    skipValues = "Z";
                printString = parentString + delimiter + x.getName() + delimiter + alias + delimiter + node.getConsolidationType().stringValue().charAt(0) + delimiter + twoPass + delimiter + timeBalance + delimiter + skipValues + delimiter + consolidation + delimiter + solveOrder + delimiter + UDA + delimiter + formula + delimiter + attribs + delimiter + System.getProperty("line.separator");
            } 
            else{
                printString = parentString + delimiter + x.getName() + delimiter + alias + delimiter + node.getConsolidationType().stringValue().charAt(0) + delimiter + twoPass + delimiter + consolidation + delimiter + solveOrder + delimiter + UDA + delimiter + formula + delimiter + attribs + delimiter + System.getProperty("line.separator");
            }
            if(isRootNode){
                String temp = "";
/*
                attributes = x.getAssociatedAttributes();
                if(attributes.getCount() > 0)
                    temp = temp + attributes.getAt(attributes.getCount() - 1).toString();
*/
                if(DimType.equalsIgnoreCase("Account"))
                    temp = "Parent" + delimiter + "Child" + delimiter + "Alias" + delimiter + "Consolidation" + delimiter + "Two pass" + delimiter + "Time balance" + delimiter + "Skip option" + delimiter + "Data storage" + delimiter + "Solve order" + delimiter + "UDA" + delimiter + "Formula" + delimiter + "Attributes";
                else
                    temp = "Parent" + delimiter + "Child" + delimiter + "Alias" + delimiter + "Consolidation" + delimiter + "Two pass" + delimiter + "Data storage" + delimiter + "Solve order" + delimiter + "UDA" + delimiter + "Formula" + delimiter + "Attributes";
                ps.println(temp);
//                ps.print(printString);
            }
            else{
                ps.flush();
                rpt.flush();
                ps.print(printString);
            }
            if(node.getLevelNumber() != 0){
            	IEssMember children[] = cv.memberSelection(node.getName(), 1, 1, dimension, "", "");
                for(int i = 0; i < children.length; i++){
            		ParseMembers_ParentChildFormat((IEssMember) children[i], node.getName(), DimType, false);
                }
            }
            return;
        }
        catch(EssException e){
            ps.flush();
            System.out.println("App encountered an exception while parsing the nodes: " + e.getNativeMessage());
            return;
        }
        catch(Exception e){
            ps.flush();
            System.out.println("Stream flush error: " + e.getMessage());
            return;
        }
    }

    // Private Methods (utilities)      
    private static boolean ValidateArgs(String[] args){
        try{
            if(args.length % 2 != 0)
                throw new Exception("\nERROR: Invalid number of arguments exception...");
            for(int i=0;i<args.length-1;i=i+2){
                if(args[i].charAt(0) == '-'){
                    switch(args[i].charAt(1)){
                        case 'e':
                            providerURL = args[i+1];
                            break;
                        case 's':
                            server = args[i+1];
                            break;
                        case 'a':
                            app = args[i+1];
                            break;
                        case 'c':
                            db = args[i+1];
                            break;
                        case 'd':
                            dimension = args[i+1];
                            break;
                        case 'm':
                            member = args[i+1];
                            break;
                        case 'u':
                            user = args[i+1];
                            break;
                        case 'p':
                            pw = args[i+1];
                            break;
                        case 'o':
                            outfile = args[i+1];
                            break;
                        case 'f':
                            outformat = args[i+1];
                            break;
                        case 'l':
                            delimiter = args[i+1];
                            break;
                        case 'q':
                        	if(args[i+1].charAt(1)=='T')
                        		quietmode = true;
                        	break;
                        default:
                            throw new Exception("\nERROR: Bad flag/argument exception...");
                    }
                }
                else
                    throw new Exception("\nERROR: Bad argument format exception...");
            }
            if((server == null) || (db == null) || (app == null) || (user == null) || (pw == null) || (providerURL == null))
                throw new Exception("\nERROR: Mandatory argument not passed...");           
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            return false;
        }

        return true;
    }
    private static void PrintUsage(){
        System.out.println("\n");
        System.out.println("*************************************************");
        System.out.println("*           OutlineExtractor Utility            *");
        System.out.println("*                                               *");
        System.out.println("*    Author: Jon Harvey                         *");
        System.out.println("*            eCapital Advisors, LLC.            *");
        System.out.println("*            jharvey@ecapitaladvisors.com       *");
        System.out.println("*                                               *");
        System.out.println("*     Built: 07/25/2010                         *");
        System.out.println("*************************************************");
        System.out.println("\nUSAGE: java -jar OutlineExtractor.jar [options]\n");
        System.out.println("Flag                Option");      
        System.out.println("===========================================================================================================================");
        System.out.println("-u       (required) Username to authenticate as");
        System.out.println("-p       (required) Password for authentication");
        System.out.println("-s       (required) Server that application resides on");
        System.out.println("-a       (required) Application to query");
        System.out.println("-c       (required) Cube/Database within the application");
        System.out.println("-e       (required) Essbase JAPI provider URL.  URL format is \"http://<APS server>:13080/aps/JAPI\"");
        System.out.println("-d       (optional) Dimension to query - used when only querying parts of the hierarchy. Default is \"\" (All dimensions)");
        System.out.println("-m       (optional) Member name to query - used when only querying parts of the hierarchy.  Default is \"\" (Root member)");
        System.out.println("-l       (optional) Character(s) to separate fields with. Default is \"|\"");
        System.out.println("-o       (optional) Output file name.  Default is \"./output.extract\"");
        System.out.println("-f       (optional) Output file format.  \"PC\" Parent-Child (default), \"GEN\" Generation" + System.getProperty("line.separator"));
        System.out.println("-q       (optional) Quiet mode (suppresses output).  \"T\" for true, \"F\" for false (default)");
        System.out.println("Arguments must be passed in a \"-flag argument\" paired format (ie - not \"OutlineExtractor.jar -upsacdme arg1 arg2 argX...\")" + System.getProperty("line.separator") + System.getProperty("line.separator"));
    }
    
}