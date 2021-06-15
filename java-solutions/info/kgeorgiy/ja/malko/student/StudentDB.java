package info.kgeorgiy.ja.malko.student;

import info.kgeorgiy.java.advanced.student.AdvancedQuery;
import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements AdvancedQuery {
    private static final Function<Student, String> getStudentName =
            (s) -> s.getFirstName() + " " + s.getLastName();

    private static final Comparator<Student> STUDENT_COMPARATOR =
            Comparator.comparing(Student::getLastName, Comparator.reverseOrder())
                    .thenComparing(Student::getFirstName, Comparator.reverseOrder())
                    .thenComparing(Student::getId);

    private static final Comparator<Group> groupComparator =
            Comparator.comparing(Group::getName);

    private <T> List<T> listMap(List<Student> students, Function<Student, T> function) {
        return students.stream().map(function).collect(Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return listMap(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return listMap(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return listMap(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return listMap(students, getStudentName);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream()
                .map(Student::getFirstName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students
                .stream()
                .max(Student::compareTo)
                .map(Student::getFirstName)
                .orElse("");
    }

    private <T> Stream<T> getByFilter(Collection<T> coll, Predicate<T> filter) {
        return coll.stream().filter(filter);
    }

    private <T> List<T> sortedList(Collection<T> coll, Comparator<T> comp) {
        return sortedList(coll, comp, (s) -> true);
    }

    private <T> List<T> sortedList(Collection<T> coll, Comparator<T> comp,
                                     Predicate<T> filter) {
        return getByFilter(coll, filter).sorted(comp).collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortedList(students, Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortedList(students, STUDENT_COMPARATOR);
    }

    public <T> List<Student> findStudentsBy(Collection<Student> students, Function<Student, T> getter, T val) {
        return sortedList(
                students,
                STUDENT_COMPARATOR,
                (s) -> val.equals(getter.apply(s)));
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudentsBy(students, Student::getFirstName, name);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentsBy(students, Student::getLastName, name);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findStudentsBy(students, Student::getGroup, group);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return getByFilter(students, (s) -> s.getGroup().equals(group))
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, BinaryOperator.minBy(String::compareTo)));
    }

    private Stream<Map.Entry<GroupName, List<Student>>> mapStudents(Collection<Student> students) {
        return mapStudents(students, Student::getGroup, Collectors.toList());
    }

    private <T, D, R> Stream<Map.Entry<T, D>> mapStudents(Collection<Student> students,
                                                          Function<Student, T> getGroupingAttribute,
                                                          Collector<Student, R, D> grouping) {
        return students.stream()
                .collect(Collectors.groupingBy(getGroupingAttribute, grouping))
                .entrySet()
                .stream();
    }

    private Stream<Group> groupStudents(Collection<Student> students,
                                        Function<List<Student>, List<Student>> studentsGetter) {
        return mapStudents(students).map((p) -> new Group(p.getKey(), studentsGetter.apply(p.getValue())));
    }

    private List<Group> getGroupsBy(Collection<Student> students, Comparator<Student> comp) {
        return groupStudents(students, (p) -> sortedList(p, comp))
                .sorted(groupComparator)
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroupsBy(students, STUDENT_COMPARATOR);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        // :NOTE: use natural ordering, instead of creating new comparators
        return getGroupsBy(students, Comparator.comparingInt(Student::getId));
    }

    private GroupName getLargestGroupBy(Collection<Student> students, ToIntFunction<Group> studentsCmp,
                                        Comparator<Group> nameCmp) {
        // :NOTE: move each comparator to constants
        return groupStudents(students, Function.identity())
                .max(Comparator.comparingInt(studentsCmp)
                        .thenComparing(nameCmp))
                .map(Group::getName)
                .orElse(null);
    }

    @Override
    public GroupName getLargestGroup(Collection<Student> students) {
        return getLargestGroupBy(
                students,
                (Group a) -> a.getStudents().size(), groupComparator);
    }

    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> students) {
        return getLargestGroupBy(students, (Group a) ->
                        (getDistinctFirstNames(a.getStudents())).size(),
                Comparator.comparing(Group::getName).reversed());
    }

    private static final Comparator<Map.Entry<String, Set<GroupName>>> nameMapComparator =
            Comparator
                    .comparing((Map.Entry<String, Set<GroupName>> s) -> s.getValue().size())
                    .thenComparing(Map.Entry::getKey);

    @Override
    public String getMostPopularName(Collection<Student> students) {
        return mapStudents(students, Student::getFirstName, Collectors.mapping(Student::getGroup, Collectors.toSet()))
                .max(nameMapComparator)
                .map(Map.Entry::getKey)
                .orElse("");
    }

    private <T> List<T> getByIndices(ArrayList<Student> students, int[] indices, Function<Student, T> infoGetter) {
        return Arrays.stream(indices)
                .mapToObj((i) -> infoGetter.apply(students.get(i)))
        .collect(Collectors.toList());
    }

    private <T> List<T> getByIndices(Collection<Student> students, int[] indices, Function<Student, T> infoGetter) {
        return getByIndices(new ArrayList<>(students), indices, infoGetter);
    }

    @Override
    public List<String> getFirstNames(Collection<Student> students, int[] indices) {
        return getByIndices(students, indices, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(Collection<Student> students, int[] indices) {
        return getByIndices(students, indices, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(Collection<Student> students, int[] indices) {
        return getByIndices(students, indices, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(Collection<Student> students, int[] indices) {
        return getByIndices(students, indices, getStudentName);
    }
}
