package com.themebuilder.backend;

import com.themebuilder.backend.config.GraphicDataInitializer;
import com.themebuilder.backend.entity.EditorAsset;
import com.themebuilder.backend.service.GraphicService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootTest
class GraphicApiTests {

    @Autowired
    private GraphicService graphicService;

    @Autowired
    private GraphicDataInitializer graphicDataInitializer;

    @Test
    void testGraphicSeedingAndEndpoints() throws Exception {
        // 1. Verify Seeding: 203 total (107 original + 96 new static)
        int[] counts = graphicDataInitializer.loadGraphicAssets();
        System.out.println("Test load counts: inserted=" + counts[0] + ", updated=" + counts[1] + ", skipped=" + counts[2]);
        Assertions.assertEquals(0, counts[2], "No assets should be skipped");

        // 2. Verify All Categories
        List<Map<String, Object>> allCategories = graphicService.getCategories();
        System.out.println("All categories count: " + allCategories.size());
        Assertions.assertEquals(18, allCategories.size(), "Should have exactly 18 categories (10 original + 8 new collections)");

        // 3. Verify Filtered Categories: Static vs Animated
        List<Map<String, Object>> staticCategories = graphicService.getCategories("static");
        System.out.println("Static categories count: " + staticCategories.size());
        Assertions.assertEquals(11, staticCategories.size(), "Should have 11 static categories (8 new + 3 original static)");

        List<Map<String, Object>> animatedCategories = graphicService.getCategories("animated");
        System.out.println("Animated categories count: " + animatedCategories.size());
        Assertions.assertEquals(7, animatedCategories.size(), "Should have 7 animated categories");

        int totalCount = 0;
        for (Map<String, Object> cat : allCategories) {
            String id = (String) cat.get("id");
            String name = (String) cat.get("name");
            int count = (int) cat.get("count");
            totalCount += count;
            System.out.println("Category: " + id + " ('" + name + "') -> " + count + " items");
            Assertions.assertTrue(count >= 10, "Each category should have at least 10 items");
        }
        Assertions.assertEquals(203, totalCount, "Total seeded graphics should be 203");

        // 4. Verify 8 New Canva-inspired Static Collections
        String[] newCollectionIds = {
                "bold-foliage",
                "organic-summer",
                "camping-rustic-drawing",
                "vivid-leaves",
                "handdrawn-animals",
                "pastel-travel-accessories",
                "simple-drawn-objects",
                "organic-sketched-fruit"
        };

        for (String colId : newCollectionIds) {
            Page<EditorAsset> colPage = graphicService.getGraphics(colId, null, "static", null, 0, 20);
            System.out.println("Verified collection '" + colId + "': " + colPage.getTotalElements() + " items");
            Assertions.assertEquals(12, colPage.getTotalElements(), "Collection " + colId + " must contain 12 items");
            for (EditorAsset asset : colPage.getContent()) {
                Assertions.assertNotNull(asset.getAssetData().get("svgContent"), "SVG content must not be null");
                Assertions.assertEquals("static", asset.getAssetData().get("type"), "Type must be static");
            }
        }

        // 5. Verify Recommended Endpoint with and without type filter
        List<EditorAsset> recommendedAll = graphicService.getRecommended(20);
        Assertions.assertTrue(recommendedAll.size() >= 15, "Should return at least 15 recommended graphics");

        List<EditorAsset> recommendedStatic = graphicService.getRecommended("static", 20);
        Assertions.assertTrue(recommendedStatic.size() >= 10, "Should return at least 10 recommended static graphics");

        List<EditorAsset> recommendedAnimated = graphicService.getRecommended("animated", 20);
        Assertions.assertTrue(recommendedAnimated.size() >= 10, "Should return at least 10 recommended animated graphics");

        // 6. Verify Search Query for Static Graphic: "monstera"
        Page<EditorAsset> searchMonstera = graphicService.getGraphics(null, "monstera", "static", null, 0, 20);
        Assertions.assertTrue(searchMonstera.getTotalElements() >= 1, "Search for 'monstera' should find at least 1 asset");
        Assertions.assertEquals("graphic-bold-foliage-monstera-leaf", searchMonstera.getContent().get(0).getAssetKey());

        // 7. Verify Search Query for Static Graphic: "campfire"
        Page<EditorAsset> searchCampfire = graphicService.getGraphics(null, "campfire", "static", null, 0, 20);
        Assertions.assertTrue(searchCampfire.getTotalElements() >= 1, "Search for 'campfire' should find matching asset");

        // 8. REGRESSION: Verify Existing Animated Categories and Items Intact
        Page<EditorAsset> arrowPage = graphicService.getGraphics("arrow-animations", null, "animated", null, 0, 20);
        Assertions.assertEquals(11, arrowPage.getTotalElements(), "Arrow Animations should have 11 items");

        Page<EditorAsset> foodPage = graphicService.getGraphics("food-animations", null, "animated", null, 0, 20);
        Assertions.assertEquals(11, foodPage.getTotalElements(), "Food Animations should have 11 items");

        Page<EditorAsset> searchPopcorn = graphicService.getGraphics(null, "popcorn", null, null, 0, 20);
        Assertions.assertTrue(searchPopcorn.getTotalElements() >= 1, "Search for 'popcorn' should find at least 1 asset");
        Assertions.assertEquals("graphic-food-popcorn-striped-box", searchPopcorn.getContent().get(0).getAssetKey());

        // 9. Single Graphic Lookup by Key
        Optional<EditorAsset> singleMonstera = graphicService.getGraphicByIdOrKey("graphic-bold-foliage-monstera-leaf");
        Assertions.assertTrue(singleMonstera.isPresent(), "Should find static graphic by assetKey");
        Assertions.assertEquals("Monstera Leaf", singleMonstera.get().getName());

        Optional<EditorAsset> singleArrow = graphicService.getGraphicByIdOrKey("graphic-arrow-curved-pulse");
        Assertions.assertTrue(singleArrow.isPresent(), "Should find animated graphic by assetKey");
        Assertions.assertEquals("Curved Pulse Arrow", singleArrow.get().getName());

        System.out.println("ALL GRAPHIC BACKEND INTEGRATION TESTS PASSED WITH 100% SUCCESS!");
    }
}
