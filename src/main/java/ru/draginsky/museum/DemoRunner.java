package ru.draginsky.museum;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "demo.enabled", havingValue = "true", matchIfMissing = true)
public class DemoRunner implements ApplicationRunner {
    private final MuseumService service;

    @Override
    public void run(ApplicationArguments args) {
        service.seed();
        show("1:N — залы и экспонаты", service.halls());
        show("N:M — маршруты экскурсий", service.routes());
        show("1:1 — карточки экспонатов", service.search(null, null, null));
        show("Запрос из имени метода: мон", service.byName("мон"));
        show("JPQL: Археология", service.byHall("Археология"));
        show("SQL: год от 1600", service.since(1600));
        show("Criteria: без фильтров", service.search(null, null, null));
        show("Criteria: Археология, год от 1000", service.search(null, "Археология", 1000));
        var year = option(args, "year-from");
        if (args.containsOption("name") || args.containsOption("hall") || year != null) {
            show("Criteria: свои фильтры", service.search(
                option(args, "name"), option(args, "hall"),
                year == null || year.isBlank() ? null : Integer.valueOf(year)
            ));
        }
        System.out.println("\nЗвёздочка: " + service.mappingCheck());
        System.out.println("Демо завершено");
    }

    private String option(ApplicationArguments args, String name) {
        var values = args.getOptionValues(name);
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    private void show(String title, List<?> items) {
        System.out.println("\n" + title);
        items.forEach(System.out::println);
    }
}
