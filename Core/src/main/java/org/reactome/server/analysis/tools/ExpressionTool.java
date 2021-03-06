package org.reactome.server.analysis.tools;

import com.martiansoftware.jsap.*;
import org.reactome.server.Main;
import org.reactome.server.analysis.core.components.EnrichmentAnalysis;
import org.reactome.server.analysis.core.data.AnalysisData;
import org.reactome.server.analysis.core.model.*;
import org.reactome.server.analysis.core.model.resource.MainResource;
import org.reactome.server.analysis.core.util.InputUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.FileInputStream;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class ExpressionTool {

    @SuppressWarnings("Duplicates")
    public static void main(String[] args) throws Exception {
        SimpleJSAP jsap = new SimpleJSAP(
                BuilderTool.class.getName(),
                "Provides a set of tools for the pathway analysis and species comparison",
                new Parameter[] {
                        new UnflaggedOption( "tool", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY,
                                "The tool to use. Options: " + Main.Tool.getOptions()) //WE DO NOT TAKE INTO ACCOUNT TOOL HERE ANY MORE
                        ,new FlaggedOption( "input", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, 'i', "input",
                                "The file containing the user data for the analysis." )
                        ,new FlaggedOption( "structure", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, 's', "structure",
                                "The file containing the data structure for the analysis." )
                        ,new FlaggedOption( "output", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, 'o', "output",
                                "The file where the results are written to." )
                        ,new QualifiedSwitch( "interactors", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, 't', "interactors",
                                "Use interactors data to perform the analysis." )
                        ,new QualifiedSwitch( "verbose", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, 'v', "verbose",
                                "Requests verbose output." )
                }
        );
        JSAPResult config = jsap.parse(args);
        if( jsap.messagePrinted() ) System.exit( 1 );

        ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");

        //Initializing Analysis Data  *** IMPORTANT ***
        String structure = config.getString("structure");
        AnalysisData analysisData = context.getBean(AnalysisData.class);
        analysisData.setFileName(structure);

        String input = config.getString("input");
        UserData ud = InputUtils.getUserData(new FileInputStream(input));

        SpeciesNode human = SpeciesNodeFactory.getHumanNode();
        boolean includeInteractors = config.getBoolean("interactors");

        EnrichmentAnalysis enrichmentAnalysis = context.getBean(EnrichmentAnalysis.class);
        HierarchiesData hierarchiesData = enrichmentAnalysis.overRepresentation(ud.getIdentifiers(), human, includeInteractors);

        for (PathwayNode node : hierarchiesData.getUniqueHitPathways(human)) {
            print(node);
        }
        if(!hierarchiesData.getNotFound().isEmpty()){
            System.out.println(String.format("%d not found in the Reactome database", hierarchiesData.getNotFound().size()));
        }

        System.out.println(":)");

    }

    private static void print(PathwayNode node){
        String name = node.getName();
        PathwayNodeData data = node.getPathwayNodeData();

        for (MainResource resource : data.getResources()) {
            Integer found = data.getEntitiesFound(resource);
            if(found==0) continue;
            Integer total = data.getEntitiesCount(resource);
            System.out.print(node.getSpecies().getName() + " >> " + resource.getName() + " >> " + name + " (" + found + "/" + total + ")");
            Double pValue = data.getEntitiesPValue(resource); Double ratio = data.getEntitiesRatio(resource); Double fdr = data.getEntitiesFDR(resource);
            if(pValue!=null && ratio!=null && fdr!=null){
                System.out.print("\t" + ratio + "\t" + pValue + "\t" + fdr);
//                DecimalFormat f = new DecimalFormat("#.####");
//                System.out.print("\t" + f.format(ratio) + "\t" + f.format(pValue));
            }

            System.out.print("\t|\t");
            found = data.getReactionsFound(resource);
            total = data.getReactionsCount(resource);
            System.out.print("[" + found + "/" + total + "]");

            System.out.print("\t|\t");
            PathwayNodeData aux = node.getPathwayNodeData();
            for (Double ev : aux.getExpressionValuesAvg(resource)) {
                System.out.print(" " + ev);
            }
            System.out.println();
        }

//        Integer found = data.getEntitiesFound();
//        Integer total = data.getEntitiesCount();
//        System.out.print(name + " (" + found + "/" + total + ")");
//        Double pValue = data.getEntitiesPValue(); Double ratio = data.getEntitiesRatio(); Double fdr = data.getEntitiesFDR();
//        if(pValue!=null && ratio!=null && fdr!=null){
//            System.out.print("\t" + ratio + "\t" + pValue + "\t" + fdr);
////            DecimalFormat f = new DecimalFormat("#.####");
////            System.out.print("\t" + f.format(ratio) + "\t" + f.format(pValue) + "\t" + fdr);
//        }
//        System.out.print("\t|\t");
//        found = data.getReactionsFound();
//        total = data.getReactionsCount();
//        System.out.print("[" + found + "/" + total + "]");
//         pValue = data.getReactionsPValue(); ratio = data.getReactionsRatio(); fdr = data.getReactionsFDR();
//        if(pValue!=null && ratio!=null && fdr!=null){
//            System.out.print("\t" + ratio + "\t" + pValue + "\t" + fdr);
////            DecimalFormat f = new DecimalFormat("#.####");
////            System.out.print("\t" + f.format(ratio) + "\t" + f.format(pValue) + "\t" + fdr);
//        }
//        System.out.print("\t|\t");
//        PathwayNodeData aux = node.getPathwayNodeData();
//        for (Double ev : aux.getExpressionValuesAvg()) {
//            System.out.print(" " + ev);
//        }
//        System.out.println();
    }
}
